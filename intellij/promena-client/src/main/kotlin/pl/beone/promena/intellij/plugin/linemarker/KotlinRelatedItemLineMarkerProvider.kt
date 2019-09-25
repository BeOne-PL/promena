package pl.beone.promena.intellij.plugin.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import pl.beone.promena.intellij.plugin.extension.getActiveFile
import pl.beone.promena.intellij.plugin.extension.isFileInAnyModule
import pl.beone.promena.transformer.contract.transformation.Transformation

class KotlinRelatedItemLineMarkerProvider : LineMarkerProvider, AbstractRelatedItemLineMarkerProvider() {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val project = element.project

        if (isKtNamedFunction(element)) {
            val ktNamedFunction = (element as KtNamedFunction)

            if (
                project.isFileInAnyModule(project.getActiveFile()) &&
                isNotInClass(ktNamedFunction) && hasNoParameters(ktNamedFunction) && isTransformationReturnType(ktNamedFunction)
            ) {
                return PromenaLineMarkerInfo(
                    element,
                    createOnClickHandler(
                        project,
                        { ktNamedFunction.getClassQualifiedName() },
                        { ktNamedFunction.name!! },
                        { getMethodComments(ktNamedFunction) }
                    )
                )
            }
        }

        return null
    }

    override fun collectSlowLineMarkers(elements: MutableList<PsiElement>, result: MutableCollection<LineMarkerInfo<PsiElement>>) {
        // deliberately omitted
    }

    private fun isKtNamedFunction(element: PsiElement): Boolean =
        element is KtNamedFunction

    private fun isNotInClass(function: KtNamedFunction): Boolean =
        function.containingClass() == null

    private fun hasNoParameters(function: KtNamedFunction): Boolean =
        function.valueParameterList?.parameters?.size == 0

    private fun isTransformationReturnType(function: KtNamedFunction): Boolean =
        function.type()?.getJetTypeFqName(false) == Transformation::class.java.canonicalName

    private fun KtNamedFunction.getClassQualifiedName(): String =
        containingKtFile.packageFqName.asString() + "." + containingKtFile.name.removeSuffix(".kt") + "Kt"

    private fun getMethodComments(function: KtNamedFunction): List<String> =
        function.bodyBlockExpression!!.children()
            .filterIsInstance<PsiComment>().map { it.text }
            .toList()
}