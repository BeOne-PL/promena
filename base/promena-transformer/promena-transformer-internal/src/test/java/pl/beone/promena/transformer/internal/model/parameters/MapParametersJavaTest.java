package pl.beone.promena.transformer.internal.model.parameters;

import kotlin.ranges.IntRange;
import org.junit.Test;
import pl.beone.lib.typeconverter.applicationmodel.exception.TypeConversionException;
import pl.beone.promena.transformer.contract.model.Parameters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MapParametersJavaTest {

    private static Parameters parameters = new MapParameters(new HashMap<>() {{
        put("int", 3);
        put("long", 10L);
        put("double", 3.5);
        put("float", 4.1f);
        put("boolean", true);
        put("string", "value");

        put("stringInt", "3");
        put("stringLong", "10");
        put("stringDouble", "3.5");
        put("stringFloat", "4.1");
        put("stringBoolean", "true");
        put("stringBoolean2", "false");

        put("parameters", new MapParameters(new HashMap<>() {{
            put("key", "value");
        }}));
        put("mapParameters", new HashMap<String, Object>() {{
            put("mapKey", "mapValue");
        }});

        put("intList", Arrays.asList(1, 2, 3));
        put("mixList", Arrays.asList(1, "string", true));
        put("stringList", Arrays.asList("1", "2", "3"));
    }});

    @Test
    public void get() {
//        Map.of()

        assertThat(parameters.get("int")).isEqualTo(3);
        assertThat(parameters.get("long")).isEqualTo(10L);
        assertThat(parameters.get("double")).isEqualTo(3.5);
        assertThat(parameters.get("float")).isEqualTo(4.1f);
        assertThat(parameters.get("boolean")).isEqualTo(true);
        assertThat(parameters.get("string")).isEqualTo("value");

        assertThatThrownBy(() -> parameters.get("absent"))
                .isExactlyInstanceOf(NoSuchElementException.class)
                .hasMessage("There is no <absent> element");
    }

    @Test
    public void getWithClass() {
        assertThat(parameters.get("int", Integer.class)).isEqualTo(3);
        assertThat(parameters.get("long", Long.class)).isEqualTo(10L);
        assertThat(parameters.get("double", Double.class)).isEqualTo(3.5);
        assertThat(parameters.get("float", Float.class)).isEqualTo(4.1f);
        assertThat(parameters.get("boolean", Boolean.class)).isEqualTo(true);
        assertThat(parameters.get("string", String.class)).isEqualTo("value");

        assertThat(parameters.get("stringInt", Integer.class)).isEqualTo(3);
        assertThat(parameters.get("stringLong", Long.class)).isEqualTo(10L);
        assertThat(parameters.get("double", Double.class)).isEqualTo(3.5);
        assertThat(parameters.get("stringFloat", Float.class)).isEqualTo(4.1f);
        assertThat(parameters.get("stringBoolean", Boolean.class)).isEqualTo(true);
        assertThat(parameters.get("stringBoolean2", Boolean.class)).isEqualTo(false);

        assertThat(parameters.get("int", Long.class)).isEqualTo(3L);
        assertThat(parameters.get("stringInt", Integer.class)).isEqualTo(3);

        assertThatThrownBy(() -> parameters.get("string", Boolean.class))
                .isExactlyInstanceOf(TypeConversionException.class)
                .hasMessage("Couldn't convert <value> (java.lang.String) to <java.lang.Boolean>");

        assertThatThrownBy(() -> parameters.get("stringBoolean", Long.class))
                .isExactlyInstanceOf(TypeConversionException.class)
                .hasMessage("Couldn't convert <true> (java.lang.String) to <java.lang.Long>");
        assertThatThrownBy(() -> parameters.get("stringInt", IntRange.class))
                .isExactlyInstanceOf(TypeConversionException.class)
                .hasMessage("Converting from <java.lang.String> to <kotlin.ranges.IntRange> isn't supported");

        assertThatThrownBy(() -> parameters.get("absent", String.class))
                .isExactlyInstanceOf(NoSuchElementException.class)
                .hasMessage("There is no <absent> element");
    }

    @Test
    public void getParameters() {
        assertThat(parameters.getParameters("parameters"))
                .isEqualTo(new MapParameters(new HashMap<>() {{
                    put("key", "value");
                }}));

        assertThatThrownBy(() -> parameters.getParameters("stringBoolean"))
                .isExactlyInstanceOf(TypeConversionException.class)
                .hasMessage(
                        "Converting from <java.lang.String> to <pl.beone.promena.transformer.contract.model.Parameters> isn't supported");
        assertThatThrownBy(() -> parameters.getParameters("mapParameters"))
                .isExactlyInstanceOf(TypeConversionException.class)
                .hasMessage(
                        "Converting from <pl.beone.promena.transformer.internal.model.parameters.MapParametersJavaTest$1$2> to <pl.beone.promena.transformer.contract.model.Parameters> isn't supported");

        assertThatThrownBy(() -> parameters.getParameters("absent"))
                .isExactlyInstanceOf(NoSuchElementException.class)
                .hasMessage("There is no <absent> element");
    }

    @Test
    public void getList() {
        assertThat(parameters.getList("intList")).isEqualTo(Arrays.asList(1, 2, 3));
        assertThat(parameters.getList("mixList")).isEqualTo(Arrays.asList(1, "string", true));
        assertThat(parameters.getList("stringList")).isEqualTo(Arrays.asList("1", "2", "3"));

        assertThatThrownBy(() -> parameters.getList("stringBoolean"))
                .isExactlyInstanceOf(TypeConversionException.class)
                .hasMessage("Converting from <java.lang.String> to <java.util.List> isn't supported");

        assertThatThrownBy(() -> parameters.getList("absent"))
                .isExactlyInstanceOf(NoSuchElementException.class)
                .hasMessage("There is no <absent> element");
    }

    @Test
    public void getListWithClass() {
        assertThat(parameters.getList("intList", Integer.class)).isEqualTo(Arrays.asList(1, 2, 3));
        assertThat(parameters.getList("stringList", String.class)).isEqualTo(Arrays.asList("1", "2", "3"));
        assertThat(parameters.getList("stringList", Long.class)).isEqualTo(Arrays.asList(1L, 2L, 3L));

        assertThatThrownBy(() -> parameters.getList("mixList", Integer.class))
                .isExactlyInstanceOf(TypeConversionException.class)
                .hasMessage("Couldn't convert <[1, string, true]> to List<java.lang.Integer>");

        assertThatThrownBy(() -> parameters.getList("absent"))
                .isExactlyInstanceOf(NoSuchElementException.class)
                .hasMessage("There is no <absent> element");
    }

    @Test
    public void getAll() {
        assertThat(parameters.getAll())
                .hasSize(17)
                .containsEntry("int", 3)
                .containsEntry("int", 3)
                .containsEntry("boolean", true)
                .containsEntry("stringFloat", "4.1")
                .containsEntry("parameters", new MapParameters(new HashMap<>() {{
                    put("key", "value");
                }}))
                .containsEntry("mixList", Arrays.asList(1, "string", true));
    }
}
