package pl.beone.promena.intellij.plugin.linemarker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.PsiMethodImpl
import com.intellij.psi.impl.source.PsiParameterImpl
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.impl.source.tree.java.PsiCodeBlockImpl
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl
import pl.beone.promena.intellij.plugin.common.getFileName
import pl.beone.promena.intellij.plugin.toolwindow.RunToolWindowTab

class PromenaRelatedItemLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>) {
        if (isIdentifier(element) && isMethod(element)) {
            val method = (element.parent as PsiMethodImpl)

            // static?
            if (isDataDescriptorParameter(method) && isTransformationReturnType(method)) {
                val comments = getMethodComments(method)

                val project = element.project
                val tabName = project.getFileName()
                val runToolWindowTab = RunToolWindowTab(project).also {
                    ApplicationManager.getApplication().invokeLater {
                        it.create(tabName)
                        it.show()
                    }
                }

                val builder = NavigationGutterIconBuilder
                    .create(AllIcons.RunConfigurations.TestState.Run)
                    .setTarget(null)
                    .setTooltipText("RUN!!!")
                result.add(builder.createLineMarkerInfo(element))
            }
        }
    }

    private fun isIdentifier(element: PsiElement): Boolean =
        element is PsiIdentifierImpl

    private fun isMethod(element: PsiElement): Boolean =
        element.parent is PsiMethodImpl

    private fun isDataDescriptorParameter(method: PsiMethodImpl): Boolean =
        method.parameters.size == 1 && method.parameters[0] is PsiParameterImpl &&
                (method.parameters[0] as PsiParameterImpl).type.canonicalText == "pl.beone.promena.transformer.contract.data.DataDescriptor"

    private fun isTransformationReturnType(method: PsiMethodImpl): Boolean =
        method.returnType?.canonicalText == "pl.beone.promena.transformer.contract.transformation.Transformation"

    private fun getMethodComments(method: PsiMethodImpl): List<String> =
        method
            .children.filterIsInstance<PsiCodeBlockImpl>().first()
            .children.filterIsInstance<PsiCommentImpl>().map { it.text }
}