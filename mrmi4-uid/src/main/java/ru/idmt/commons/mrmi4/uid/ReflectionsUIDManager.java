package ru.idmt.commons.mrmi4.uid;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.idmt.commons.mrmi4.commons.RObject;
import ru.idmt.commons.mrmi4.commons.UIDManager;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectionsUIDManager implements UIDManager {
	private final Map<Short, Class> classByUid = new HashMap<Short, Class>();
	private final Map<Class, Short> uidByClass = new HashMap<Class, Short>();

	private final Map<Short, Method> methodByUid = new HashMap<Short, Method>();
	private final Map<Method, Short> uidByMethod = new HashMap<Method, Short>();

	public ReflectionsUIDManager(URL resource) throws ParserConfigurationException, URISyntaxException, IOException, SAXException, ClassNotFoundException, NoSuchMethodException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document document = builder.parse(resource.toURI().getPath());
		Element root = document.getDocumentElement();

		NodeList list = root.getChildNodes();
		for (int index = 0; index < list.getLength(); index++) {
			Node classNode = list.item(index);

			if (classNode instanceof Element) {
				Element classElement = (Element) classNode;

				if ("class".equals(classElement.getTagName())) {
					String className = classElement.getElementsByTagName("name").item(0).getTextContent();
					short classUID = Short.valueOf(classElement.getElementsByTagName("index").item(0).getTextContent());

					Class clazz = Class.forName(className);

					classByUid.put(classUID, clazz);
					uidByClass.put(clazz, classUID);

					Node methodsNode = classElement.getElementsByTagName("Methods").item(0);
					if (methodsNode instanceof Element) {
						Element methodsElement = (Element) methodsNode;

						NodeList methodsList = methodsElement.getElementsByTagName("Method");

						for (int j = 0; j < methodsList.getLength(); j++) {
							Node methodNode = methodsList.item(j);
							if (methodNode instanceof Element) {
								Element methodElement = (Element) methodNode;
								String methodName = methodElement.getElementsByTagName("name").item(0).getTextContent();
								short methodUID = Short.valueOf(methodElement.getElementsByTagName("index").item(0).getTextContent());

								Element parameterTypesElement = (Element) methodElement.getElementsByTagName("parameterTypes").item(0);

								List<Class> types = new ArrayList<Class>();
								NodeList parameterTypeList = parameterTypesElement.getElementsByTagName("class");
								for (int k = 0; k < parameterTypeList.getLength(); k++) {
									Node typeNode = parameterTypeList.item(k);

									if (typeNode instanceof Element) {
										Class type = Class.forName(typeNode.getTextContent().trim());
										types.add(type);
									}
								}

								Method method = clazz.getMethod(methodName, types.toArray(new Class[types.size()]));
								methodByUid.put(methodUID, method);
								uidByMethod.put(method, methodUID);
							}
						}
					}
				}
			}
		}
	}

	public short getClassUID(Class<? extends RObject> iClass) {
		if (!uidByClass.containsKey(iClass)) {
			throw new IllegalArgumentException("no such class: " + iClass);
		}
		return uidByClass.get(iClass);
	}

	public Class<?> getClassByUID(short classUID) {
		if (!classByUid.containsKey(classUID)) {
			throw new IllegalArgumentException("no such class UID: " + classUID);
		}
		return classByUid.get(classUID);
	}

	public short getMethodUID(Method method) {
		if (!uidByMethod.containsKey(method)) {
			throw new IllegalArgumentException("no such method: " + method);
		}
		return uidByMethod.get(method);
	}

	public Method getMethodByUID(short methodUID) {
		if (!methodByUid.containsKey(methodUID)) {
			throw new IllegalArgumentException("no such method UID: " + methodUID);
		}
		return methodByUid.get(methodUID);
	}
}
