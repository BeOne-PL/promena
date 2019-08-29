package pl.beone.promena.intellij.plugin.common

import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.psi.KtNamedFunction

fun PsiMethod.getClassQualifiedName(): String =
    containingClass!!.qualifiedName!!

fun KtNamedFunction.getClassQualifiedName(): String =
    containingKtFile.packageFqName.asString() + "." + containingKtFile.name.removeSuffix(".kt") + "Kt"
