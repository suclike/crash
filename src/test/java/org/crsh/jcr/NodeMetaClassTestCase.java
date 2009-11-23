/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.crsh.jcr;

import groovy.lang.GroovyShell;
import junit.framework.TestCase;
import org.crsh.RepositoryBootstrap;
import org.crsh.jcr.NodeMetaClass;

import javax.jcr.Repository;
import javax.jcr.Session;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NodeMetaClassTestCase extends TestCase
{

   /** Make sure we integrate the node meta class system with Groovy. */
   static
   {
      NodeMetaClass.setup();
   }

   /** . */
   private Repository repo;

   /** . */
   private boolean initialized = false;

   /** . */
   private Session session;

   /** . */
   private GroovyShell shell;

   @Override
   protected void setUp() throws Exception
   {
      if (!initialized)
      {
         RepositoryBootstrap bootstrap = new RepositoryBootstrap();
         bootstrap.bootstrap();
         repo = bootstrap.getRepository();
         initialized = true;
      }

      //
      session = repo.login();

      //
      shell = new GroovyShell();
      shell.setVariable("session", session);
   }

   @Override
   protected void tearDown() throws Exception
   {
      session.logout();

      //
      repo = null;
      session = null;
      shell = null;
   }

   public void testNodeGetName() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "Node root = session.getRootNode();\n" +
         "Node foo = root.addNode('foo');\n" +
         "assert foo.name == 'foo';\n");
   }

   public void testMissingMethod() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "Node root = session.getRootNode();\n" +
         "try {\n" +
         "  root.someNonExistingMethod();\n" +
         "  assert false;\n" +
         "}\n" +
         "catch (MissingMethodException e) { }\n");
   }

   public void testChildNode() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "Node root = session.getRootNode();\n" +
         "Node foo = root.addNode('foo');\n" +
         "assert root.foo == foo;\n");
   }

   public void testEach() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "Node root = session.getRootNode();\n" +
         "Node foo = root.addNode('foo');\n" +
         "def nodes = [:];\n" +
         "root.each({ node ->\n" +
         "  nodes[node.name] = node;\n" +
         "});\n" +
         "assert nodes.containsKey('foo');\n");
   }

   public void testEachWithIndex() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "Node root = session.getRootNode();\n" +
         "Node foo = root.addNode('foo');\n" +
         "def nodes = [:];\n" +
         "root.eachWithIndex({ node, index ->\n" +
         "  nodes[node.name] = node;\n" +
         "});\n" +
         "assert nodes.containsKey('foo');\n");
   }

   public void testNodeSubscript() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "Node root = session.getRootNode();\n" +
         "Node foo = root.addNode('foo');\n" +
         "def nodes = [:];\n" +
         "root.getNodes().eachWithIndex({ Node child, int index ->\n" +
         "  assert child == root[index];\n" +
         "  int indexComplement = index - root.getNodes().size();\n" +
         "  assert child == root[indexComplement];\n" +
         "});\n");
   }

   public void testEachProperty() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "import javax.jcr.Property;\n" +
         "Node root = session.getRootNode();\n" +
         "root.setProperty('bar', 'bar_value');\n" +
         "def properties = [:];\n" +
         "root.eachProperty({ property ->\n" +
         "  properties[property.name] = property;\n" +
         "});\n" +
         "assert properties.containsKey('bar');\n");
   }

   public void testPropertyNamePrefix() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "import javax.jcr.Property;\n" +
         "Node root = session.getRootNode();\n" +
         "Node res = root.addNode('res', 'nt:resource');\n" +
         "res['jcr:encoding'] = 'foo_encoding';\n" + 
         "assert res['jcr:encoding'] == 'foo_encoding';\n" +
         "Property encodingProperty = res.getProperty('jcr:encoding');\n" +
         "assert encodingProperty != null;\n" +
         " assert 'foo_encoding' == encodingProperty.getString();\n");
   }

   public void testNodeNamePrefix() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "Node root = session.getRootNode();\n" +
         "Node file = root.addNode('file', 'nt:file');\n" +
         "Node content = file.addNode('jcr:content', 'nt:base');\n" +
         "assert content == file['jcr:content'];\n");
   }

   public void testByte1() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "import javax.jcr.Property;\n" +
         "Node root = session.getRootNode();\n" +
         "byte barByte = 123;\n" +
         "root.barByte = barByte;\n" +
         "assert root.barByte instanceof Long;\n" +
         "assert root.barByte == 123L;\n" +
         "Property barByteProperty = root.getProperty('barByte');\n" +
         "assert barByteProperty != null;\n" +
         "assert 123L == barByteProperty.getLong();\n");
   }

   public void testInteger1() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "import javax.jcr.Property;\n" +
         "Node root = session.getRootNode();\n" +
         "int barInteger = 123;\n" +
         "root.barInteger = barInteger;\n" +
         "assert root.barInteger instanceof Long;\n" +
         "assert root.barInteger == 123L;\n" +
         "Property barIntegerProperty = root.getProperty('barInteger');\n" +
         "assert barIntegerProperty != null;\n" +
         "assert 123L == barIntegerProperty.getLong();\n");
   }

   public void testBigInteger1() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "import javax.jcr.Property;\n" +
         "Node root = session.getRootNode();\n" +
         "BigInteger barInteger = 123;\n" +
         "root.barInteger = barInteger;\n" +
         "assert root.barInteger instanceof Long;\n" +
         "assert root.barInteger == 123L;\n" +
         "Property barIntegerProperty = root.getProperty('barInteger');\n" +
         "assert barIntegerProperty != null;\n" +
         "assert 123L == barIntegerProperty.getLong();\n");
   }

   public void testString1() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "import javax.jcr.Property;\n" +
         "Node root = session.getRootNode();\n" +
         "String barString = '123';\n" +
         "root.barString = barString;\n" +
         "assert root.barString instanceof String;\n" +
         "assert root.barString == '123';\n" +
         "Property barStringProperty = root.getProperty('barString');\n" +
         "assert barStringProperty != null;\n" +
         "assert '123' == barStringProperty.getString();\n");
   }

   public void testChar1() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "import javax.jcr.Property;\n" +
         "Node root = session.getRootNode();\n" +
         "char barCharacter = 'c';\n" +
         "root.barCharacter = barCharacter;\n" +
         "assert root.barCharacter instanceof String;\n" +
         "assert root.barCharacter == 'c';\n" +
         "Property barCharacterProperty = root.getProperty('barCharacter');\n" +
         "assert barCharacterProperty != null;\n" +
         "assert 'c' == barCharacterProperty.getString();\n");
   }

   public void testBoolean1() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "import javax.jcr.Property;\n" +
         "Node root = session.getRootNode();\n" +
         "boolean barBoolean = true;\n" +
         "root.barBoolean = barBoolean;\n" +
         "assert root.barBoolean instanceof Boolean;\n" +
         "assert root.barBoolean == true;\n" +
         "Property barBooleanProperty = root.getProperty('barBoolean');\n" +
         "assert barBooleanProperty != null;\n" +
         "assert true == barBooleanProperty.getBoolean();\n");
   }

   public void testCalendar1() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "import javax.jcr.Property;\n" +
         "import java.util.Calendar;\n" +
         "Node root = session.getRootNode();\n" +
         "Calendar now = Calendar.getInstance();\n" +
         "root.barCalendar = now;\n" +
         "assert root.barCalendar instanceof Calendar;\n" +
         "assert root.barCalendar == now;\n" +
         "Property barCalendarProperty = root.getProperty('barCalendar');\n" +
         "assert barCalendarProperty != null;\n" +
         "assert now == barCalendarProperty.getDate();\n");
   }

   public void testDouble1() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "import javax.jcr.Property;\n" +
         "Node root = session.getRootNode();\n" +
         "double barDouble = 0.5D;\n" +
         "root.barDouble = barDouble;\n" +
         "assert root.barDouble instanceof Double;\n" +
         "assert root.barDouble == 0.5D;\n" +
         "Property barDoubleProperty = root.getProperty('barDouble');\n" +
         "assert barDoubleProperty != null;\n" +
         "assert 0.5D == barDoubleProperty.getDouble();\n");
   }

   public void testFloat1() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "import javax.jcr.Property;\n" +
         "Node root = session.getRootNode();\n" +
         "float barFloat = 0.5D;\n" +
         "root.barFloat = barFloat;\n" +
         "assert root.barFloat instanceof Double;\n" +
         "assert root.barFloat == 0.5D;\n" +
         "Property barFloatProperty = root.getProperty('barFloat');\n" +
         "assert barFloatProperty != null;\n" +
         "assert 0.5D == barFloatProperty.getDouble();\n");
   }

   public void testDecimal1() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         "import javax.jcr.Property;\n" +
         "Node root = session.getRootNode();\n" +
         "BigDecimal barBD = new BigDecimal(0.5);\n" +
         "root.barBD = barBD;\n" +
         "assert root.barBD instanceof Double;\n" +
         "assert root.barBD == 0.5D;\n" +
         "Property barBDProperty = root.getProperty('barBD');\n" +
         "assert barBDProperty != null;\n" +
         "assert 0.5D == barBDProperty.getDouble();\n");
   }

   public void testBilto() throws Exception
   {
      shell.evaluate("" +
         "import javax.jcr.Node;\n" +
         ";\n" +
         ";\n");
   }
}
