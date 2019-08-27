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
import pl.beone.promena.intellij.plugin.common.*

class KotlinRelatedItemLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val project = element.project
        val activeFile = project.getActiveFile()

        if (isKtNamedFunction(element)) {
            val ktNamedFunction = (element as KtNamedFunction)

            if (
                project.isFileInAnyModule(activeFile) &&
                isNotInClass(ktNamedFunction) && isDataDescriptorParameter(ktNamedFunction) && isTransformationReturnType(ktNamedFunction)
            ) {
                return PromenaLineMarkerInfo(
                    element,
                    createOnClickHandler(
                        project,
                        project.getModule(activeFile),
                        ktNamedFunction.getClassQualifiedName(),
                        ktNamedFunction.name!!,
                        { getMethodComments(ktNamedFunction) }
                    )
                )
            }
        }

        return null
    }

    private fun getMethodComments(function: KtNamedFunction): List<String> =
        function.bodyBlockExpression!!.children()
            .filterIsInstance<PsiComment>().map { it.text }
            .toList()

    override fun collectSlowLineMarkers(elements: MutableList<PsiElement>, result: MutableCollection<LineMarkerInfo<PsiElement>>) {
        // deliberately omitted
    }

    private fun isKtNamedFunction(element: PsiElement): Boolean =
        element is KtNamedFunction

    private fun isNotInClass(function: KtNamedFunction): Boolean =
        function.containingClass() == null

    private fun isDataDescriptorParameter(function: KtNamedFunction): Boolean =
        function.valueParameterList?.parameters?.size == 1 &&
                function.valueParameterList!!.parameters[0]?.type()?.getJetTypeFqName(false) == "pl.beone.promena.transformer.contract.data.DataDescriptor"

    private fun isTransformationReturnType(function: KtNamedFunction): Boolean =
        function.type()?.getJetTypeFqName(false) == "pl.beone.promena.transformer.contract.transformation.Transformation"
}