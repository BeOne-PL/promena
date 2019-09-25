package pl.beone.promena.intellij.plugin.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.*
import org.jetbrains.kotlin.psi.psiUtil.parents
import pl.beone.promena.intellij.plugin.extension.getActiveFile
import pl.beone.promena.intellij.plugin.extension.isFileInAnyModule
import pl.beone.promena.transformer.contract.transformation.Transformation

class JavaRelatedItemLineMarkerProvider : LineMarkerProvider, AbstractRelatedItemLineMarkerProvider() {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val project = element.project
        val activeFile = project.getActiveFile()

        if (isMethod(element)) {
            val psiMethod = (element.parent as PsiMethod)

            if (
                project.isFileInAnyModule(activeFile) &&
                startsWithPromena(psiMethod) &&
                isNotInInnerClass(psiMethod) &&
                isPublicStatic(psiMethod) &&
                hasNoParameters(psiMethod) &&
                isTransformationReturnType(psiMethod)
            ) {
                return PromenaLineMarkerInfo(
                    element,
                    createOnClickHandler(
                        project,
                        { psiMethod.getClassQualifiedName() },
                        { psiMethod.name },
                        { getMethodComments(psiMethod) }
                    )
                )
            }
        }

        return null
    }

    override fun collectSlowLineMarkers(elements: MutableList<PsiElement>, result: MutableCollection<LineMarkerInfo<PsiElement>>) {
        // deliberately omitted
    }

    private fun isMethod(element: PsiElement): Boolean =
        element is PsiIdentifier && element.parent is PsiMethod

    private fun startsWithPromena(method: PsiMethod): Boolean =
        method.name.startsWith("promena", true)

    private fun isNotInInnerClass(psiMethod: PsiMethod): Boolean =
        psiMethod.parents
            .filterIsInstance<PsiClass>()
            .count() == 1

    private fun isPublicStatic(method: PsiMethod): Boolean =
        method.modifierList.hasExplicitModifier("public") && method.modifierList.hasExplicitModifier("static")

    private fun hasNoParameters(method: PsiMethod): Boolean =
        method.parameters.isEmpty()

    private fun isTransformationReturnType(method: PsiMethod): Boolean =
        method.returnType?.canonicalText == Transformation::class.java.canonicalName

    private fun PsiMethod.getClassQualifiedName(): String =
        containingClass!!.qualifiedName!!

    private fun getMethodComments(method: PsiMethod): List<String> =
        method
            .children.filterIsInstance<PsiCodeBlock>().first()
            .children.filterIsInstance<PsiComment>().map { it.text }
}