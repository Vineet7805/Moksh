package com.github.moksh.generator.core;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassMetaData {

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Set<String> getExtends() {
		return extend;
	}

	public void addExtends(String extend) {
		this.extend.add(extend);
	}

	public Set<String> getAnnotations() {
		return annotations;
	}

	public void addAnnotation(String annotation) {
		this.annotations.add(annotation);
	}

	public List<Property> getProperties() {
		return properties;
	}
	
	public String[] getRequiredNonPrimitiveProps() {
		//properties
		//String csv="";
		List<String> listOfRequiredParams=new ArrayList<String>();
		for (Property prop : properties) {
			//if(prop.required)
			//System.out.println(prop.name +" is required:"+prop.required+" and primitive:"+prop.primitive+" with type:"+prop.type);
			if(prop.required && !prop.primitive && !prop.type.startsWith("List<")) {
				//System.out.println(prop.name +" is required:"+prop.required+" and primitive:"+prop.primitive+" with type:"+prop.type);
				listOfRequiredParams.add(prop.type);
			}
		}
		
		return listOfRequiredParams.toArray(new String[listOfRequiredParams.size()]);
	}

	public Property addProperty(String access, String type, String name,boolean primitive) {
		Property prop = getProperty(name);
		if (prop == null) {
			prop = new Property(access, type, name,primitive);
			properties.add(prop);
		}
		return prop;
	}

	public Property getProperty(String name) {
		return properties.stream().filter(obj -> name.equals(obj.name)).findAny().orElse(null);
	}

	public Set<String> getImplement() {
		return implement;
	}

	public void addImplement(String implement) {
		this.implement.add(implement);
	}

	public Set<String> getImports() {
		return imports;
	}

	public void addImport(String imprt) {
		this.imports.add(imprt);
	}

	public List<Function> getFunctions() {
		return functions;
	}

	public Function getFunction(String name) {
		return functions.stream().filter(obj -> name.equals(obj.name)).findAny().orElse(null);
	}

	public Function addFunction(String access, String returnType, String name) {
		Function fun = getFunction(name);
		if (fun == null) {
			fun = new Function(access, returnType, name);
			this.functions.add(fun);
		}
		return fun;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getModelType() {
		return modelType;
	}

	public void setModelType(String modelType) {
		if(modelType.toLowerCase().contains("api") && api==null)
			api=new API();
		this.modelType = modelType;
	}

	private String name;
	private String access;
	private String type;
	private Set<String> extend;
	private Set<String> annotations;
	private List<Property> properties;
	private Set<String> implement;
	private Set<String> imports;
	private List<Function> functions;
	private String modelType;
	private String description;
	private String title;
	private API api=null;
	private String scope;
	private String root=null;
	private String customCode="";
	private boolean isRequired=true;
	
	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	public API getAPI() {
		return api;
	}
	
	public ClassMetaData(String access, String name, String type) {
		this.name = name;
		this.type = type;
		this.access = access;
		extend = new HashSet<String>();
		annotations = new HashSet<String>();
		implement = new HashSet<String>();
		imports = new HashSet<String>();
		properties = new ArrayList<ClassMetaData.Property>();
		functions = new ArrayList<ClassMetaData.Function>();
		//root=Math.random()+"Name"+System.currentTimeMillis();
	}

	public class API{
		public String code;
		public String method;
		public String path;
		public String custom;
		public String imports;
		public String version;
		public String operationName;
		public Set<String> queryParams;
		public String packageName;
		public void addQueryParam(String name,String type) {
			if(queryParams.stream().filter(param-> param.toLowerCase().equals((name+":"+type).toLowerCase())).findAny().orElse(null)!=null)
				queryParams.remove((name+":"+type));
			queryParams.add((name+":"+type).toLowerCase());
		}
		public Set<String> getImportList(){
			if(imports==null || imports.trim().length()==0)
				return null;
			String importedClasses=(";"+(new String(Base64.getDecoder().decode(this.imports)).trim())).replaceAll("[\\n\\t\\r ]", "");
			System.out.println(importedClasses);
			String imports[]=importedClasses.split(";import");
			Set<String> importSet=new HashSet<String>();
			for (String imprt : imports) {
				if(imprt.trim().length()>0) {
					importSet.add(("import "+imprt+";").replace(";;", ";"));
					//System.out.println("import "+imprt+";");
				}
			}
			return importSet;
		}
	}
	public class Property {
		public String name;
		public String type;
		public boolean primitive = true;
		public String access;
		public String description;
		public boolean required = false;
		public Set<String> annotations;
		public String title;
		public String value;
		public String scope;

		public Property(String access, String type, String name,boolean primitive) {
			this.type = type;
			this.name = name;
			this.access = access;
			this.primitive=primitive;
			annotations=new HashSet<String>();
		}
		
		public boolean removeAnnotation(String matchName) {
			return annotations.removeIf(annot-> annot.contains(matchName));
		}
		
		@Override
		public String toString() {
			String data="";
			//Adding annotations
			for (String line : annotations) {
				data+=line+"\r\n";
			}
			if(required) 
				data+="@NotEmpty(message = \""+name+" must not be empty\")\r\n";
			if(description!=null)
				data+="@Description(value = \""+description+"\")\r\n";
			
			data+=access+" "+type+" "+name+";\r\n";
			
			return data;
		}
	}

	public class Function {
		public String name;
		public String returnType;
		public String access;
		public Set<String> annotations;
		public List<String> codeLines;
		public List<Param> params;
		public Set<String> exceptions;
		public String description;
		public String title;
		@Override
		public String toString() {
			String data="";
			//Adding annotations
			for (String line : annotations) {
				data+=line+"\r\n";
			}
			data+=access+" "+returnType+" "+name+"(";
			if(params!=null && params.size()>0) {
				for (Param prm : params) {
					data+=prm.toString()+",";
				}
				data+=",";data=data.replace(",,", "");
			}
			data+=")";
			//Add exceptions
			if(exceptions!=null && exceptions.size()>0) {
				data+=" throws ";
				for (String excp : exceptions) {
					data+=excp+",";
				}
				data+=",";data=data.replace(",,", "");
			}
			data+= "{\r\n";
			//Adding code
			for (String line : codeLines) {
				data+=line+"\r\n";
			}
			data+="}";
			data+= "\r\n";
			return data;
		}
		public Function(String access, String returnType, String name) {
			this.name = name;
			this.returnType = returnType;
			this.access = access;
			annotations = new HashSet<String>();
			exceptions = new HashSet<String>();
			codeLines = new ArrayList<String>();
			params = new ArrayList<ClassMetaData.Function.Param>();
		}

		public Param addParam(String annotation, String type, String name) {
			Param param = getParam(name);
			if (param == null) {
				param = new Param(annotation, type, name);
				params.add(param);
			}
			return param;
		}

		public Param getParam(String name) {
			return params.stream().filter(prmm -> name.equals(prmm.name)).findAny().orElse(null);
		}

		class Param {
			public String name;
			public String type;
			public Set<String> annotations;

			public Param(String annotation, String type, String name) {
				this.name = name;
				this.type = type;
				annotations = new HashSet<String>();
				if (annotation != null)
					annotations.add(annotation);
			}
			
			@Override
			public String toString() {
				String data="";
				//Adding annotations
				if(annotations!=null && annotations.size()>0)
				for (String line : annotations) {
					data+=line+" ";
				}
				data+=type+" "+name;
				return data;
			}
		}
	}
	@Override
	public String toString() {
		String data="";
		
		if(!"local".equalsIgnoreCase(getScope())) {
			//Adding imports
			for (String line : imports) {
				data+=line+"\r\n";
			}
		}
		data+="\r\n";
		//Adding annotations
		if(annotations!=null)
		for (String line : annotations) {
			data+=line+"\r\n";
		}
		//Creating class
		data+=access+" "+type+" "+name+" ";
		if(extend!=null && extend.size()>0)
			data+="extends "+extend.iterator().next();
		data+=" {\r\n";
		data+="\r\n";
		//Adding properties to class
		if(properties!=null)
		for (Property prop : properties) {
			data+=prop.toString()+"\r\n";
		}
		data+="\r\n";
		data+="//*********************custom code**********************\r\n";
		data+=customCode+"\r\n";
		data+="//********************************************************\r\n";
		//Adding functions to class
		if(functions!=null)
		for (Function func : functions) {
			data+=func.toString()+"\r\n";
		}
		data+="\r\n";
		data+="}\r\n";
		return data;
	}
	
	public String exportJava(String dirPath,String packageName) throws Exception {
		if(getName().endsWith("Resource"))
			System.out.println("Main resource '"+getName()+"' is required="+isRequired);
		if("local".equalsIgnoreCase(getScope()) || "api".equalsIgnoreCase(getModelType()) || (getName().endsWith("Resource") && isRequired==false))
			return null;
		if (packageName == null || packageName.trim().length() == 0) {
			return ("Please specify base package name like com.example.model");
		}
		String data ="";
		data = "package " + packageName + ";\r\n";
		data+=toString();
		
		FileOutputStream fos = new FileOutputStream(new File(dirPath + "/" + name + ".java"));
		fos.write(data.getBytes());
		fos.flush();
		fos.close();
		System.out.println("Exported: "+getRoot());
		return null;
	}
	
	public String exportGUI(String dirPath, String nav) throws Exception {
		dirPath=dirPath.split("java")[0]+"resources/static/admin-ui/";
		String data=nav;
		if("entity".equalsIgnoreCase(getModelType())) {
				data=nav.replace("%"+getName()+"listUrl%", "http://localhost:8080/"+CodeGen.getPlural(getName().toLowerCase()))
				.replace("%"+getName()+"editUrl%", "http://localhost:8080/"+CodeGen.getPlural(getName().toLowerCase())+"/");//.replace("%navigation%", navigation);
//			File file=new File(dirPath);
//			if(!file.exists() || !file.isDirectory()) {
//				file.mkdir();
//			}
//			FileOutputStream fos = new FileOutputStream(
//					new File(dirPath + getName().toLowerCase() + ".html"));
//			fos.write(data.getBytes());
//			fos.flush();
//			fos.close();
		}
		return data;
	}
	
	public String exportTypeScript1(String dirPath,String packageName) throws Exception {
		if (packageName == null || packageName.trim().length() == 0) {
			return ("Please specify base package name like com.example.model");
		}
		String data = "";
		//Creating class
		data+="export "+type+" "+name+" {\r\n";
		
		//Adding properties to class
		for (Property prop : properties) {
			if(!prop.type.startsWith("List<") && prop.primitive && !prop.name.equals("id"))
				data+=prop.name+": "+prop.type.replace("java.util.", "").replace("Double", "number").replace("long", "number").replace("Integer", "number")+";\r\n";
		}

		data+="}";
		
		File file=new File(dirPath+"/TypeScript");
		if(!file.exists() || !file.isDirectory()) {
			file.mkdir();
		}
		FileOutputStream fos = new FileOutputStream(
				new File(dirPath + "/TypeScript/" + name + ".ts"));
		fos.write(data.getBytes());
		fos.flush();
		fos.close();
		return null;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	
	private String exportLocal(String dirPath,String packageName,Map<String, ClassMetaData> classes,ClassMetaData resource) throws Exception {
		ClassMetaData cmd=null;
		if(properties!=null && properties.size()>0) {
			addAnnotation("@AllArgsConstructor");
			addImport("import lombok.AllArgsConstructor;");
			addAnnotation("@NoArgsConstructor");
			addImport("import lombok.NoArgsConstructor;");
			for (Property prop : properties) {
				if(prop.required)
					addImport("import javax.validation.constraints.NotEmpty;");
				if(!prop.primitive) {
					cmd=classes.get(root+"."+prop.type.replace("List<", "").replace(">", ""));
					System.out.println("Find root: '"+root+"."+prop.type.replace("List<", "").replace(">", "")+"'");
					resource.addImport("import "+packageName+"."+prop.type.replace("List<", "").replace(">", "")+"."+prop.type.replace("List<", "").replace(">", "")+";");
					if("local".equalsIgnoreCase(cmd.getScope())) {
						addImport("import "+packageName+"."+cmd.getName()+".*;");
						cmd.exportLocal(dirPath+"/"+cmd.getName(), packageName+"."+cmd.getName(), classes,resource);
						//dirPath=dirPath+"/"+getName();
						//data+=newData;
					}
				}
			}
		}
	
		String data="";
		data = "package " + packageName + ";\r\n";
		//Adding imports
		for (String line : imports) {
			data+=line+"\r\n";
		}
		data+="\r\n";
		//Adding annotations
		if(annotations!=null)
		for (String line : annotations) {
			data+=line+"\r\n";
		}
		//Creating class
		data+="public "+type+" "+name+" ";
		if(extend!=null && extend.size()>0)
			data+="extends "+extend.iterator().next();
		data+=" {\r\n";
		data+="\r\n";
		//Adding properties to class
		if(properties!=null)
		for (Property prop : properties) {
			data+=prop.toString()+"\r\n";
		}
		data+="\r\n";
		//Adding functions to class
		if(functions!=null)
		for (Function func : functions) {
			data+=func.toString()+"\r\n";
		}
		data+="\r\n";
		
		
		data+="}\r\n";
		
		
		File dir=new File(dirPath);
		if(!dir.exists())
			dir.mkdirs();
		if(!"api".equalsIgnoreCase(getModelType())) {
			FileOutputStream fos = new FileOutputStream(
					new File(dirPath + "/" + name + ".java"));
			fos.write(data.getBytes());
			fos.flush();
			fos.close();
		}
		return data;
	}
	
	public void exportAPI(String dirPath,String packageName,Map<String, ClassMetaData> classes) throws Exception {	
		ClassMetaData resource=classes.get(getName()+"Resource");
		exportLocal(dirPath+"/"+getName(),packageName+"."+getName(),classes,resource);
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String randomID) {
		this.root = randomID;
	}

	public String getCustomCode() {
		return customCode;
	}

	public void setCustomCode(String customCode) {
		this.customCode = customCode;
	}
	String json="";
	private void appendln(String str) {
		json+=str+"\n";
	}
	private void append(String str) {
		json+=str;
	}
	public String identifier="-~-~-~-010-~-~-~";
	public String toJsonSchema(Map<String, ClassMetaData> classes) {
		//appendln("\""+getName()+"\":{");
		for (Property prop : properties) {
			if(prop.primitive) {
				if(prop.type.contains("[]")) {
					appendln("\""+prop.name+"\":{");
					appendln("\"type\" : \"array\",");
					appendln("\"items\": [{");
					appendln("\"type\" : \""+prop.type.replace("[]", "")+"\"");
					appendln("}]");
					append("},");
				}else {
					appendln("\""+prop.name+"\":{");
					appendln("\"type\" : \""+prop.type.toLowerCase()+"\"");
					append("},");
				}
			}else {
				if(prop.type.contains("List<")) {
					appendln("\""+prop.type.replace("List<", "").replace(">", "")+"\":{");
					appendln("\"type\":\"array\",");
					appendln("\"items\": {");
					appendln("\"type\":\"object\",");
					appendln("\"properties\":{");
					appendln(classes.get(getRoot()+"."+(prop.type.replace("List<", "").replace(">", ""))).toJsonSchema(classes));
					appendln("}");
					appendln("}");
					append("},");
				}else {
					appendln("\""+prop.type+"\":{");
					appendln("\"type\":\"object\",");
					appendln("\"properties\":{");
					appendln(classes.get(getRoot()+"."+(prop.type)).toJsonSchema(classes));
					appendln("}");
					append("},");
				}
			}
		}
		appendln(identifier);
		String tempJson=json;
		json="";
		return tempJson;
	}
}
