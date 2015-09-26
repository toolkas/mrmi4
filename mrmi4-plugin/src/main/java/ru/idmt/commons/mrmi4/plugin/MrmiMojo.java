package ru.idmt.commons.mrmi4.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.idmt.commons.mrmi4.commons.RObject;
import ru.idmt.maven.commons.MavenUtils;
import ru.idmt.maven.commons.provider.ArtifactFileProvider;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @goal process
 * @threadSafe true
 */
public class MrmiMojo extends AbstractMojo {
	/**
	 * @parameter expression="${project}"
	 * @readonly
	 * @required
	 */
	private MavenProject project;

	/**
	 * @component
	 */
	private ArtifactResolver resolver;

	/**
	 * @parameter default-value="${localRepository}"
	 */
	private ArtifactRepository localRepository;

	/**
	 * @parameter default-value="${project.remoteArtifactRepositories}"
	 */
	private List remoteRepositories;

	/**
	 * @component
	 */
	private ArtifactMetadataSource artifactMetadataSource;

	/**
	 * @parameter default-value="${target}"
	 * @required
	 */
	private File target;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			Artifact artifact = project.getArtifact();
			resolver.resolve(artifact, remoteRepositories, localRepository);

			final Map<String, File> dependencies = MavenUtils.getDependenciesMap(project, resolver, localRepository, remoteRepositories, artifactMetadataSource, null, new ArtifactFileProvider());

			File output = new File(project.getBuild().getOutputDirectory());

			final Set<URL> urls = new HashSet<URL>();
			urls.add(output.toURI().toURL());

			for (File file : dependencies.values()) {
				urls.add(file.toURI().toURL());
			}

			for (URL url : urls) {
				getLog().debug("url = " + url);
			}

			final URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());

			List<Class> classes = new ArrayList<Class>();
			processDirectory(loader, classes, output, output);

			saveInfo(classes);
		} catch (Exception ex) {
			getLog().error(ex);
			throw new MojoExecutionException(ex.getMessage(), ex);
		}
	}

	private void processDirectory(URLClassLoader loader, List<Class> classes, File directory, File output) throws ClassNotFoundException, IOException {
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					processDirectory(loader, classes, file, output);
				} else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
					String className = output.toURI().relativize(file.toURI()).getPath();
					className = className.substring(0, className.length() - ".class".length());
					className = className.replaceAll("/", ".");

					Class clazz = loader.loadClass(className);
					classes.add(clazz);
				}
			}
		}
	}

	private void saveInfo(List<Class> classes) throws IOException, ParserConfigurationException, TransformerException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document document = builder.newDocument();
		Element root = document.createElement("Reflections");

		int index = 0;
		for (Class clazz : classes) {
			if (RObject.class.isAssignableFrom(clazz)) {
				Element classElement = (Element) root.appendChild(document.createElement("class"));
				classElement.appendChild(document.createElement("name")).appendChild(document.createTextNode(clazz.getName()));
				classElement.appendChild(document.createElement("index")).appendChild(document.createTextNode(String.valueOf(++index)));

				Element methodsElement = (Element) classElement.appendChild(document.createElement("Methods"));
				for (Method method : clazz.getMethods()) {
					Element methodElement = (Element) methodsElement.appendChild(document.createElement("Method"));
					methodElement.appendChild(document.createElement("name")).appendChild(document.createTextNode(method.getName()));
					methodElement.appendChild(document.createElement("index")).appendChild(document.createTextNode(String.valueOf(++index)));

					Element parameterTypesElement = (Element) methodElement.appendChild(document.createElement("parameterTypes"));
					for (Class<?> parameterType : method.getParameterTypes()) {
						parameterTypesElement.appendChild(document.createElement("class")).appendChild(document.createTextNode(parameterType.getName()));
					}
				}
			}
		}

		document.appendChild(root);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(target);

		transformer.transform(source, result);

		getLog().info("Metadata saved to " + target);
	}
}
