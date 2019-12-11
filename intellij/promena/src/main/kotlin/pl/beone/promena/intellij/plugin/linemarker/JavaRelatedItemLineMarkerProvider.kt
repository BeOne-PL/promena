package pl.beone.promena.intellij.plugin.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.psi.psiUtil.parents
import pl.beone.promena.intellij.plugin.extension.getActiveFileOrNull
import pl.beone.promena.intellij.plugin.extension.isFileInAnyModule
import pl.beone.promena.transformer.contract.transformation.Transformation

internal class JavaRelatedItemLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val project = element.project
        val activeFile = project.getActiveFileOrNull() ?: return null

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
                return JavaPromenaLineMarkerInfo(psiMethod)
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
        method.parameterList.parametersCount == 0

    private fun isTransformationReturnType(method: PsiMethod): Boolean =
        method.returnType?.canonicalText == Transformation::class.java.canonicalName
}