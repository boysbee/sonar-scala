/*
 * SonarQube
 * Copyright (C) 2011-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.scala.cpd;

import static org.junit.Assert.assertEquals;


import java.io.File;

import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokens;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class ScalaTokenizerTest {

    @Before
    public void init() {
        TokenEntry.clearImages();
    }

    @Test
    public void test1() throws Throwable {
        Tokenizer tokenizer = new ScalaTokenizer();
        SourceCode sourceCode = new SourceCode(new SourceCode.FileCodeLoader(resourceToFile("/cpd/NewlineToken.scala"), "UTF-8"));
        Tokens tokens = new Tokens();
        tokenizer.tokenize(sourceCode, tokens);
        
        assertEquals(19, tokens.size());
    }
    
    @Test
    public void test2() throws Throwable {
        Tokenizer tokenizer = new ScalaTokenizer();
        SourceCode sourceCode = new SourceCode(new SourceCode.FileCodeLoader(resourceToFile("/cpd/NewlinesToken.scala"), "UTF-8"));
        Tokens tokens = new Tokens();
        tokenizer.tokenize(sourceCode, tokens);
        
        assertEquals(24, tokens.size());
    }
    
    @Test
    public void test3() throws Throwable {    	
    	Tokenizer tokenizer = new ScalaTokenizer();
        SourceCode sourceCode = new SourceCode(new SourceCode.FileCodeLoader(resourceToFile("/cpd/Empty.scala"), "UTF-8"));
        Tokens tokens = new Tokens();
        tokenizer.tokenize(sourceCode, tokens);
        
        assertEquals(1, tokens.size());
    }
    
    @Test
    public void test4() throws Throwable {    	
    	Tokenizer tokenizer = new ScalaTokenizer();
        SourceCode sourceCode = new SourceCode(new SourceCode.FileCodeLoader(resourceToFile("/cpd/NewlinesToken.scala"), "UTF-8"));
        Tokens tokens = new Tokens();
        tokenizer.tokenize(sourceCode, tokens);
        
        assertEquals("class NewlinesToken {", sourceCode.getSlice(1, 1));
    }

    private File resourceToFile(String path) {
        return FileUtils.toFile(getClass().getResource(path));
    }

}
