package org.test;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

public class DocTest {

	@Test
	public void test() throws DocumentException {
		SAXReader reader = new SAXReader();
		String xml = "<iq id=\"NdHaI-4\" to=\"目标\" from=\"用户\" type=\"send\"><chat xmlns=\"jabber:iq:chat\"><content>内容</content></chat></iq>";
		Document document = reader.read(new StringReader(xml));
		Element root = document.getRootElement();
		Element chat = root.element("chat");
		System.out.println(chat.getName());
	}
}
