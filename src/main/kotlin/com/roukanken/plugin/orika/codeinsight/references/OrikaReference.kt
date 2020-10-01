package com.roukanken.plugin.orika.codeinsight.references

import com.intellij.patterns.PsiJavaPatterns
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

internal class OrikaReference(element: PsiLiteral) : PsiReferenceBase<PsiLiteral>(element) {

    private val fieldMapping = PsiTreeUtil.getParentOfType(this.element, PsiMethodCallExpression::class.java)

    private val mappingMethod: PsiElement? = PsiTreeUtil.collectElements(this.fieldMapping) {
        PsiJavaPatterns.psiMethod().withName("classMap").accepts(element)
    }.firstOrNull()

    override fun resolve(): PsiElement? {
        return null
    }

    companion object {
        fun create(psiLiteral: PsiLiteral): Array<PsiReference> {
            return arrayOf(OrikaReference(psiLiteral))
        }
    }
}