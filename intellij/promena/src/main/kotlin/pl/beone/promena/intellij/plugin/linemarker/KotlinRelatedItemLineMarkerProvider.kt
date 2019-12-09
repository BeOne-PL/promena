package pl.beone.promena.intellij.plugin.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import pl.beone.promena.intellij.plugin.applicationmodel.ClassDescriptor
import pl.beone.promena.intellij.plugin.extension.getActiveFileOrNull
import pl.beone.promena.intellij.plugin.extension.isFileInAnyModule
import pl.beone.promena.transformer.contract.transformation.Transformation

internal class KotlinRelatedItemLineMarkerProvider : LineMarkerProvider, AbstractRelatedItemLineMarkerProvider() {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val project = element.project
        val activeFile = project.getActiveFileOrNull() ?: return null

        if (isKtNamedFunction(element)) {
            val ktNamedFunction = (element as KtNamedFunction)

            if (
                project.isFileInAnyModule(activeFile) &&
                startsWithPromena(ktNamedFunction) &&
                isNotInClass(ktNamedFunction) &&
                isPublic(ktNamedFunction) &&
                hasNoParameters(ktNamedFunction) &&
                isTransformationReturnType(ktNamedFunction)
            ) {
                return PromenaLineMarkerInfo(
                    element,
                    createOnClickHandler(
                        project,
                        { getMethodComments(ktNamedFunction) },
                        { ClassDescriptor(getPackageName(ktNamedFunction), getClassName(ktNamedFunction), getFunctionName(ktNamedFunction)) })
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

    private fun startsWithPromena(function: KtNamedFunction): Boolean =
        function.nameAsSafeName.asString().startsWith("promena", true)

    private fun isNotInClass(function: KtNamedFunction): Boolean =
        function.containingClass() == null

    private fun isPublic(ktNamedFunction: KtNamedFunction): Boolean =
        ktNamedFunction.isPublic

    private fun hasNoParameters(function: KtNamedFunction): Boolean =
        function.valueParameterList?.parameters?.size == 0

    private fun isTransformationReturnType(function: KtNamedFunction): Boolean =
        function.type()?.getJetTypeFqName(false) == Transformation::class.java.canonicalName

    private fun getPackageName(function: KtNamedFunction): String =
        (function.containingFile as KtFile).packageFqName.asString()

    private fun getClassName(function: KtNamedFunction): String =
        try {
            function.containingKtFile.name
        } catch (e: Exception) {
            error("Couldn't get class name")
        }.removeSuffix(".kt") + "Kt"

    private fun getFunctionName(function: KtNamedFunction): String =
        function.name!!

    private fun getMethodComments(function: KtNamedFunction): List<String> =
        try {
            (function.bodyBlockExpression!! as ASTNode).children()
                .filterIsInstance<PsiComment>().map(PsiElement::getText)
                .toList()
        } catch (e: KotlinNullPointerException) {
            emptyList()
        }
}