package railo.loader.osgi.factory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import railo.loader.engine.CFMLEngineFactory;
import railo.loader.engine.CFMLEngineFactorySupport;
import railo.loader.osgi.BundleVersion;
import railo.loader.util.Util;

public class BundleBuilderFactory {
	
	/*
TODO

Export-Package: org.wikipedia.helloworld;version="1.0.0"
Import-Package: org.osgi.framework;version="1.3.0"

Export-Package: Expresses which Java packages contained in a bundle will be made available to the outside world.
Import-Package: Indicates which Java packages will be required from the outside world to fulfill the dependencies needed in a bundle.
	 * */
	
	//Indicates the OSGi specification to use for reading this bundle.
	private static final int MANIFEST_VERSION=2;

	private final String name;
	private final String symbolicName;
	private String description;
	private BundleVersion bundleVersion;
	
	public BundleVersion getBundleVersion() {
		return bundleVersion;
	}

	public void setBundleVersion(String version) {
		if(Util.isEmpty(version,true))return ;
		this.bundleVersion=new BundleVersion(version);
		
		System.out.println(version+"->"+bundleVersion.toString());
	}
	private String activator;

	private List<File> jars=new ArrayList<File>();

	private List<String> exportPackage;
	private List<String> importPackage; 
	private List<String> dynImportPackage; 
	private List<String> classPath; 

	/**
	 * 
	 * @param symbolicName this entry specifies a unique identifier for a bundle, based on the reverse domain name convention (used also by the java packages).
	 * @param name Defines a human-readable name for this bundle, Simply assigns a short name to the bundle. 
	 * @param description A description of the bundle's functionality. 
	 * @param version Designates a version number to the bundle.
	 * @param activator Indicates the class name to be invoked once a bundle is activated.
	 * @param name 
	 * @throws BundleBuilderFactoryException 
	 */
	public BundleBuilderFactory(String name, String symbolicName) throws BundleBuilderFactoryException{
		if(Util.isEmpty(symbolicName)) {
			if(Util.isEmpty(name))
				throw new BundleBuilderFactoryException("symbolic name is reqired");
			symbolicName=toSymbolicName(name);
		}
		this.name=name;
		this.symbolicName=symbolicName;
		
	}

	private String toSymbolicName(String name) {
		name=name.replace(' ', '.');
		name=name.replace('_', '.');
		name=name.replace('-', '.');
		return name;
	}
	
	public List<String> getExportPackage() {
		return exportPackage;
	}

	public void addExportPackage(String strExportPackage) {
		if(Util.isEmpty(strExportPackage)) return;
		if(exportPackage==null)exportPackage=new ArrayList<String>();
		addPackages(exportPackage,strExportPackage);
		
	}
	
	private static void addPackages(List<String> packages, String str) {
		StringTokenizer st=new StringTokenizer(str,",");
		while(st.hasMoreTokens()){
			packages.add(st.nextToken().trim());
		}
	}

	public List<String> getImportPackage() {
		return importPackage;
	}

	public List<String> getDynamicImportPackage() {
		return dynImportPackage;
	}

	public void addImportPackage(String strImportPackage) {
		if(Util.isEmpty(strImportPackage)) return;
		if(importPackage==null)importPackage=new ArrayList<String>();
		addPackages(importPackage,strImportPackage);
	}
	
	public void addDynamicImportPackage(String strDynImportPackage) {
		if(Util.isEmpty(strDynImportPackage)) return;
		if(dynImportPackage==null)dynImportPackage=new ArrayList<String>();
		addPackages(dynImportPackage,strDynImportPackage);
	}

	public List<String> getClassPath() {
		return classPath;
	}

	public void addClassPath(String str) {
		if(classPath==null)classPath=new ArrayList<String>();
		classPath.add(str);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getActivator() {
		return activator;
	}

	public void setActivator(String activator) {
		this.activator = activator;
	}

	private String buildManifestSource(List<String> jarsUsed){
		StringBuilder sb=new StringBuilder();
			sb.append("Bundle-ManifestVersion: ").append(MANIFEST_VERSION).append('\n');
		if(!Util.isEmpty(name))
			sb.append("Bundle-Name: ").append(name).append('\n');
			sb.append("Bundle-SymbolicName: ").append(symbolicName).append('\n');
		if(!Util.isEmpty(description))
			sb.append("Bundle-Description: ").append(description).append('\n');
		if(bundleVersion!=null)
			sb.append("Bundle-Version: ").append(bundleVersion.toString()).append('\n');
		
		if(!Util.isEmpty(activator)) {
			addImportPackage("org.osgi.framework");
			sb.append("Bundle-Activator: ").append(activator).append('\n');
		}
		addPackage(sb,"Export-Package",exportPackage);
		addPackage(sb,"Import-Package",importPackage);
		addPackage(sb,"DynamicImport-Package",dynImportPackage);
		addPackage(sb,"Bundle-ClassPath",classPath);
		// TODO :
		try {
			log(new File(".").getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log(sb.toString());
		return sb.toString();// NL at the end is needed, so no trim
	}

	private void addPackage(StringBuilder sb,String label, List<String> pack) {
		if(pack!=null && pack.size()>0) {
			sb.append(label).append(": ");
			Iterator<String> it = pack.iterator();
			boolean first=true;
			while(it.hasNext()) {
				if(!first) {
					sb.append(',');
				}
				sb.append(it.next());
				first=false;
			}
			sb.append('\n');
		}
	}

	public void addJar(File jar){
		log("add "+jar+" to the bundle");
		jars.add(jar);
	}
	
	public void build(File target) throws IOException {
		OutputStream os = new FileOutputStream(target);
		try{
			build(os);
		}
		finally {
			CFMLEngineFactory.closeEL(os);
		}
	}
	
	public void build(OutputStream os) throws IOException {
		Charset charset = Charset.forName("UTF-8");
		ZipOutputStream zos=new MyZipOutputStream(os,charset);
		try{
		
			
			// jars
			List<String> jarsUsed=new ArrayList<String>();
			{
				File jar;
				Iterator<File> it = jars.iterator();
				while(it.hasNext()){
					jar=it.next();
					log("jar:"+jar.getName());
					jarsUsed.add(jar.getName());
					handleEntry(zos,jar, new JarEntryListener(zos));
				}
			}
			
			// manifest
			String mani = buildManifestSource(jarsUsed);
			InputStream is=new ByteArrayInputStream(mani.getBytes(charset));
			ZipEntry ze=new ZipEntry("META-INF/MANIFEST.MF");
			zos.putNextEntry(ze);
	        try {
	            copy(is,zos);
	        } 
	        finally {
	        	CFMLEngineFactorySupport.closeEL(is);
	            zos.closeEntry();
	        }
		
		
		
		}
		finally {
			CFMLEngineFactorySupport.closeEL(zos);
		}
	}
	

	private void handleEntry(ZipOutputStream target, File file, EntryListener listener) throws IOException {
		ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
		try{
			ZipEntry entry;
			while((entry=zis.getNextEntry())!=null){
				listener.handleEntry(file,zis,entry);
				zis.closeEntry();
			}
		}
		finally {
			CFMLEngineFactorySupport.closeEL(zis);
		}
	}

	class JarEntryListener implements EntryListener {

		private ZipOutputStream zos;

		public JarEntryListener(ZipOutputStream zos) { 
			this.zos=zos;
		}

		@Override
		public void handleEntry(File zipFile,ZipInputStream source,ZipEntry entry) throws IOException {
			System.out.println("- "+entry.getName());
			// manifest
			if("META-INF/MANIFEST.MF".equalsIgnoreCase(entry.getName())) {
				ByteArrayOutputStream baos=new ByteArrayOutputStream();
				copy(source,baos);
				//log(zipFile+" -> META-INF/MANIFEST.MF");
				//log(new String(baos.toByteArray()));
				return;
			}
			
			// ignore the following stuff
			if(entry.getName().endsWith(".DS_Store")||
					entry.getName().startsWith("__MACOSX")) {
				return;
			}
			
			
			MyZipEntry ze=new MyZipEntry(entry.getName());
			ze.setComment(entry.getComment());
			ze.setTime(entry.getTime());
			ze.setFile(zipFile);
			
    		try {
    			zos.putNextEntry(ze);
    		}
    		catch(NameAlreadyExistsException naee){
    			if(entry.isDirectory()) {
    				return;
    			}
    			log("--------------------------------");
    			log(ze.getName());
    			log("before:"+naee.getFile());
    			log("curren:"+zipFile);
    			log("size:"+naee.getSize()+"=="+entry.getSize());
        		return; // TODO throw naee;
    		}
            try {
                copy(source,zos);
            } 
            finally {
                zos.closeEntry();
            }
			
		}

		
	}
	
	
	public interface EntryListener {

		public void handleEntry(File zipFile, ZipInputStream source, ZipEntry entry) throws IOException;

	}
	
	public class MyZipOutputStream extends ZipOutputStream {
		
		private Map<String,File> names=new HashMap<String,File>();
		
		public MyZipOutputStream(OutputStream out,Charset charset) {
			super(out);
		}
		
		
		@Override
		public void putNextEntry(ZipEntry e) throws IOException {
			File file = names.get(e.getName());
			if(names.containsKey(e.getName()))
				throw new NameAlreadyExistsException(e.getName(),file,e.getSize());
			
			if(e instanceof MyZipEntry)names.put(e.getName(),((MyZipEntry)e).getFile());
			super.putNextEntry(e);
		}
		
	}
	
	public class MyZipEntry extends ZipEntry {

		private File file;
		
		public MyZipEntry(String name) {
			super(name);
		}
		public void setFile(File file) {
			this.file=file;
		}
		public MyZipEntry(ZipEntry e) {
			super(e);
		}
		
		public File getFile(){
			return file;
		}
	}

	private final static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[0xffff];
        int len;
        while((len = in.read(buffer)) !=-1)
          out.write(buffer, 0, len);
    }
	public void log(String str) {
		System.out.println(str);
	}
	

	/*public static void test(String[] jars, String trg) throws Exception {
		File target=new File(trg);
		BundleBuilderFactory bf = new BundleBuilderFactory("Test", "test", "", "1.0.0", null);
		for(int i=0;i<jars.length;i++){
			bf.addJar(caster.toResource(jars[i]));
		}
		bf.build(target);
		
		
	}*/
}