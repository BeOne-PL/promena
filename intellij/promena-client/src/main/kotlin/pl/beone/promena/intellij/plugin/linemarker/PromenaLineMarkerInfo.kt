package pl.beone.promena.intellij.plugin.linemarker

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement

class PromenaLineMarkerInfo<T : PsiElement>(
    element: T,
    private val executeOnClick: () -> Unit
) : LineMarkerInfo<T>(
    element,
    element.textRange,
    AllIcons.RunConfigurations.TestState.Run,
    Pass.LINE_MARKERS,
    null,
    null,
    GutterIconRenderer.Alignment.LEFT
) {

    override fun createGutterRenderer(): GutterIconRenderer =
        object : LineMarkerGutterIconRenderer<T>(this) {

            override fun getClickAction(): AnAction? =
                object : AnAction("Transform using HTTP", null, AllIcons.RunConfigurations.TestState.Run) {
                    override fun actionPerformed(e: AnActionEvent) {
                        executeOnClick()
                    }
                }

            override fun getTooltipText(): String? {
                return "Run 'Transform using HTTP'"
            }

            override fun isNavigateAction(): Boolean =
                true

            override fun getPopupMenuActions(): ActionGroup? =
                null
        }
}