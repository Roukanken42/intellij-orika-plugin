package com.roukanken.plugin.orika.codeinsight.references

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.psi.PsiFile
import com.roukanken.plugin.orika.OrikaCompletionTestCase
import org.intellij.lang.annotations.Language
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.map
import strikt.assertions.toList

private const val MAPPER = """
    package com.example.mapper;
    
    import com.example.dto.Example;
    import com.example.dto.ExampleDto;
    import ma.glasnost.orika.MapperFacade;
    import ma.glasnost.orika.MapperFactory;
    import ma.glasnost.orika.impl.DefaultMapperFactory;
    
    public class Config {
        public static MapperFacade buildMapperFacade() {
            MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
    
            mapperFactory.classMap(Example.class, ExampleDto.class)
                    %s
                    .byDefault()
                    .register();
            
            return mapperFactory.getMapperFacade();
        }
    }
"""

internal class OrikaReferenceTest : OrikaCompletionTestCase() {

    override fun setUp() {
        super.setUp()

        addDirectoryToProject("dto")
    }

    override fun getTestDataPath(): String {
        return "src/test/testData"
    }

    fun testStuff() {
        val mapping = ".field(\"<caret>\", \"\")\n"
        val mapper: String = String.format(MAPPER, mapping)
        val file = configureMapperByText(mapper)

        val subject: Array<LookupElement> = myFixture.completeBasic()
        expectThat(subject).toList()
            .map { LookupElementPresentation.renderElement(it).itemText }
            .contains("anInt", "anString")

//        val elementAt = file!!.findElementAt(myFixture.caretOffset - 1)
//        assertThat(elementAt)
//            .isNotNull()
//            .isInstanceOf(PsiIdentifier::class.java)
//        assertThat(elementAt!!.text).isEqualTo("Collections")
//
//        val references = ReferenceProvidersRegistry.getReferencesFromProviders(elementAt!!)
//        assertThat(references).isEmpty()
    }

    private fun configureMapperByText(@Language("java") text: String): PsiFile? {
        return myFixture.configureByText(JavaFileType.INSTANCE, text)
    }
}