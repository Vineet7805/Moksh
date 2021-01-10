package com.github.moksh.generator.core;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.moksh.generator.core.ClassMetaData.API;
import com.github.moksh.generator.core.ClassMetaData.Function;
import com.github.moksh.generator.core.ClassMetaData.Property;

public class CodeGen {
	//public static CodeGen CG = new CodeGen();
	public Map<String, ClassMetaData> classes;
	// public static final String LOCATION = "D:\\nvme\\data\\CodeGen";

	private CodeGen() {
		classes = new HashMap<String, ClassMetaData>();
	}
	public static CodeGen getInstance() {
		return new CodeGen();
	}
	public ClassMetaData createEntityClass(String name, String scope,String root) {
		if (scope != null && scope.trim().length() > 0)
			scope = scope.toLowerCase().trim();
		if(!scope.equals("local"))
			root=name;
		if (classes.get(root) != null)
			return classes.get(root);
		ClassMetaData clazz = new ClassMetaData("public", name, "class");
		clazz.addAnnotation("@Data");
		clazz.addImport("import lombok.Data;");
		clazz.addImport("import org.springframework.data.rest.core.annotation.Description;");
		clazz.setScope(scope);
		if ("persist".equals(scope)) {
			clazz.addAnnotation("@Entity");
			Property prop = clazz.addProperty("public", "long", "id", true);
			prop.annotations.add("@Id");
			//prop.annotations.add("@JsonIgnore");
			prop.annotations.add("@GeneratedValue(strategy = GenerationType.AUTO)");
			prop.required=false;
			clazz.addImport("import javax.persistence.Entity;");
			clazz.addImport("import javax.persistence.GeneratedValue;");
			clazz.addImport("import javax.persistence.GenerationType;");
			clazz.addImport("import javax.persistence.Id;");
			clazz.addImport("import javax.validation.constraints.NotEmpty;");
			clazz.addImport("import com.fasterxml.jackson.annotation.JsonIgnore;");
			clazz.addImport("import org.springframework.hateoas.EntityModel;");
			clazz.addExtends("EntityModel<"+clazz.getName()+">");
			clazz.setModelType("Entity");
			createRepositoryInterface(name);
			createResourceClass(name);
			
		}
		System.out.println(root);
		clazz.setRoot(root);
		System.out.println(clazz.getName());
		classes.put(root, clazz);
		return clazz;
	}

	public ClassMetaData createAPIClass(String name) {
		if (classes.get(name) != null)
			return classes.get(name);
		ClassMetaData clazz = new ClassMetaData("public", name, "class");
		clazz.setModelType("API");
		clazz.setScope("Global");
		ClassMetaData clazzResource =createResourceClass(name);
		clazz.setRoot(name);
		classes.put(name, clazz);
		return clazz;
	}

	public ClassMetaData createRepositoryInterface(String name) {
		String entityClass = name;
		name += "Repository";
		if (classes.get(name) != null)
			return classes.get("name");
		ClassMetaData clazz = new ClassMetaData("public", name, "interface");
		clazz.addExtends("PagingAndSortingRepository<" + entityClass + ", Long>");
		clazz.addAnnotation("@Repository");

		clazz.addImport("import org.springframework.data.repository.PagingAndSortingRepository;");
		clazz.addImport("import org.springframework.stereotype.Repository;");
		clazz.setScope("Global");
		clazz.setRoot(name);
		classes.put(name, clazz);
		return clazz;
	}

	public ClassMetaData createResourceClass(String name) {
		String entityClass = name;
		name += "Resource";
		if (classes.get(name) != null)
			return classes.get("name");
		ClassMetaData clazz = new ClassMetaData("public", name, "class");
		clazz.addAnnotation("@RestController");
		clazz.addAnnotation("@CrossOrigin");
		clazz.addAnnotation("@Slf4j");
		clazz.addImport("import org.springframework.web.bind.annotation.CrossOrigin;");
		clazz.addImport("import java.net.URI;");
		clazz.addImport("import java.util.List;");
		clazz.addImport("import java.util.Optional;");
		clazz.addImport("import org.springframework.beans.factory.annotation.Autowired;");
		clazz.addImport("import org.springframework.data.jpa.repository.JpaRepository;");
		clazz.addImport("import org.springframework.data.repository.query.Param;");
		clazz.addImport("import org.springframework.http.ResponseEntity;");
		clazz.addImport("import org.springframework.stereotype.Repository;");
		clazz.addImport("import org.springframework.web.bind.annotation.GetMapping;");
		clazz.addImport("import org.springframework.web.bind.annotation.PathVariable;");
		clazz.addImport("import org.springframework.web.bind.annotation.PostMapping;");
		clazz.addImport("import org.springframework.web.bind.annotation.RequestBody;");
		clazz.addImport("import org.springframework.web.bind.annotation.RequestParam;");
		clazz.addImport("import org.springframework.web.bind.annotation.RestController;");
		clazz.addImport("import org.springframework.web.servlet.support.ServletUriComponentsBuilder;");
		clazz.addImport("import org.springframework.data.repository.PagingAndSortingRepository;");
		clazz.addImport("import org.springframework.data.rest.core.annotation.RepositoryRestResource;");
		clazz.addImport("import java.util.Collection;");
		clazz.addImport("import java.util.ArrayList;");
		clazz.addImport("import lombok.extern.slf4j.Slf4j;");
		clazz.setScope("Global");
		clazz.setRoot(name);
		classes.put(name, clazz);
		return clazz;
	}

	public ClassMetaData addListGetMapping(ClassMetaData clazz, String entity) {
		String name = clazz.getName().replace("Resource", "");
		Property prop = clazz.addProperty("private ", entity + "Repository", entity.toLowerCase() + "Repo", false);
		prop.annotations.add("@Autowired");
		Function func = clazz.addFunction("public", "List<" + entity + ">", "get" + getPlural(entity));

		String path = getPlural(name.toLowerCase());
		if (name.equals(entity)) {
			func.annotations.add("@GetMapping(path = \"/" + path + "\")");
			func.codeLines.add("List<" + entity + "> fetched="+"(List<" + entity + ">)" + prop.name + ".findAll();");
			func.codeLines.add("fetched.forEach(obj-> obj.generateLinks());");
			func.codeLines.add("if(fetched.size()==0) fetched.add(new "+entity+"());");
			func.codeLines.add("return fetched;");
		} else {
			path = path + "/{id}/" + getPlural(entity.toLowerCase());
			func.annotations.add("@GetMapping(path = \"/" + path + "\")");
			func.addParam("@PathVariable", "long", "id");
			func.exceptions.add("Exception");
			func.codeLines.add(
					"Optional<" + name + "> " + name.toLowerCase() + "=" + name.toLowerCase() + "Repo.findById(id);");
			func.codeLines.add("if(" + name.toLowerCase() + ".isPresent()){ ");
			func.codeLines.add("List<" + entity + "> " + getPlural(entity.toLowerCase()) + "=" + name.toLowerCase() + ".get().get"
					+ getPlural(entity) + "();");
			func.codeLines.add("if(" + getPlural(entity.toLowerCase()) + "==null || " + getPlural(entity.toLowerCase()) + ".size()==0){");
			func.codeLines.add(getPlural(entity.toLowerCase()) + "=new ArrayList<"+entity+">();"+getPlural(entity.toLowerCase()) + ".add(new "+entity+"());}else");
			func.codeLines.add(getPlural(entity.toLowerCase())+ ".forEach(obj-> obj.generateLinks());\r\n");
			func.codeLines.add("return " + getPlural(entity.toLowerCase())  + ";\r\n}");
			// func.codeLines.add("return "+entity.toLowerCase()+";\r\n}");
			func.codeLines.add("throw new Exception(\"" + name + " not found\");");
		}
		return clazz;
	}

	public ClassMetaData addGetMapping(ClassMetaData clazz, String entity) {
		String name = clazz.getName().replace("Resource", "");
		Property prop = clazz.addProperty("private ", entity + "Repository", entity.toLowerCase() + "Repo", false);
		prop.annotations.add("@Autowired");
		Function func = clazz.addFunction("public", entity, "get" + entity);
		String path = getPlural(name.toLowerCase()) + "/{id}";
		func.addParam("@PathVariable", "long", "id");
		if (name.equals(entity)) {
			func.annotations.add("@GetMapping(path = \"/" + path + "\")");
			func.exceptions.add("Exception");
			func.codeLines.add("Optional<" + entity + "> " + entity.toLowerCase() + "=" + prop.name + ".findById(id);");
			func.codeLines
					.add("if(!" + entity.toLowerCase() + ".isPresent())");
			func.codeLines.add("throw new Exception(\"" + entity + " not found\");");
			func.codeLines.add(entity+" fetched=" + entity.toLowerCase() + ".get();");
			func.codeLines.add("fetched.generateLinks();");
			func.codeLines.add("return fetched;");
		} else {
			func.annotations.add("@GetMapping(path = \"/" + path + "/" + getPlural(entity.toLowerCase()) + "\")");
			func.exceptions.add("Exception");
			func.codeLines.add(
					"Optional<" + name + "> " + name.toLowerCase() + "=" + name.toLowerCase() + "Repo.findById(id);");
			func.codeLines.add("if(" + name.toLowerCase() + ".isPresent()){ ");
			func.codeLines.add(
					entity + " " + entity.toLowerCase() + "_tmp=" + name.toLowerCase() + ".get().get" + entity + "();");
			func.codeLines.add("if(" + entity.toLowerCase() + "_tmp==null)");
			func.codeLines.add(entity.toLowerCase() + "_tmp=new "+entity+"();");
			func.codeLines.add(entity.toLowerCase() + "_tmp.generateLinks();\r\n");
			func.codeLines.add("return " + entity.toLowerCase() + "_tmp;\r\n}");
			func.codeLines.add("throw new Exception(\"" + name + " not found\");");
		}
		return clazz;
	}
	
	public static String getPlural(String word) {
		// Consonant at the 2nd last position?
		boolean b = "bcdfghjklmnpqrstvwxyzs".contains(String.valueOf(word.charAt(word.length()-2))); 

		// Substring for cases where last letter needs to be replaced
		String x = word.substring(0,word.length()-1);

		if(word.endsWith("s") || word.endsWith("x") || word.endsWith("z") || word.endsWith("ch") || word.endsWith("sh"))
		    return word + "es";
		if(word.endsWith("y") && b)
		    return x + "ies";
		if(word.endsWith("f")) 
		    return x + "ves";
		if(word.endsWith("fe"))
		    return word.substring(0,word.length()-2) + "ves";
		if(word.endsWith("o") && b)
		    return word + "es";

		return word += "s";
	}
	public ClassMetaData addDeleteMapping(ClassMetaData clazz) {
		String name = clazz.getName().replace("Resource", "");
		String objectName = name.toLowerCase();
		String path = getPlural(objectName) + "/{id}";
		Property prop = clazz.addProperty("private ", name + "Repository", name.toLowerCase() + "Repo", false);
		prop.annotations.add("@Autowired");
		if (clazz.getFunction("delete" + name) != null)
			return null;

		Function func = clazz.addFunction("public", "void", "detele" + name);
		func.exceptions.add("Exception");

		clazz.addImport("import org.springframework.web.bind.annotation.DeleteMapping;");
		func.annotations.add("@DeleteMapping(path = \"/" + path + "\")");
		func.addParam("@PathVariable", "long", "id");
		func.exceptions.add("Exception");
		func.codeLines.add("Optional<" + name + "> " + name.toLowerCase() + "=" + name.toLowerCase() + "Repo.findById(id);");
		func.codeLines
				.add("if(" + name.toLowerCase() + ".isPresent()) " + name.toLowerCase() + ".get(); else");
		func.codeLines.add("throw new Exception(\"" + name + " not found\");");
		func.codeLines.add(name.toLowerCase() + "Repo.deleteById(id);");
		return clazz;
	}
	
	public ClassMetaData addPatchMapping(ClassMetaData clazz, String entity) {
		String name = clazz.getName().replace("Resource", "");
		String entityObject = entity.toLowerCase();
		String objectName = name.toLowerCase();
		String path = getPlural(objectName);
		path += "/{id}/" + getPlural(entityObject);
		//System.out.println("'Path=\"/" + path + "\"" + params + "'");
		ClassMetaData entityClass=classes.get(entity);
		List<Property> entityProperties=entityClass.getProperties();
		Property uniqueProperty=null;
		for (Property property : entityProperties) {
			Set<String> annots=property.annotations;
			for (String annot : annots) {
				if(annot.contains("@Column(unique = true")) {
					uniqueProperty=property;
					break;
				}
			}
			if(uniqueProperty!=null)
				break;
		}
		if(uniqueProperty!=null) {
			
			Property property=clazz.addProperty("private", "EntityManager", "entityManager", false);
			property.annotations.add("@PersistenceContext");
			clazz.addImport("import javax.persistence.EntityManager;"); 
			clazz.addImport("import javax.persistence.PersistenceContext;");
			clazz.addImport("import org.springframework.web.bind.annotation.PatchMapping;");
			clazz.addImport("import javax.persistence.TypedQuery;");
			Function addRel = clazz.addFunction("public", entity, "addRelTo" + entity);
			addRel.exceptions.add("Exception");
			addRel.annotations.add("@PatchMapping(path = \"/" + path + "\")");
			addRel.addParam("@RequestBody", entity, entityObject);
			addRel.addParam("@PathVariable", "long", "id");
			addRel.codeLines.add(
					"Optional<" + name + "> " + name.toLowerCase() + "=" + name.toLowerCase() + "Repo.findById(id);");
			addRel.codeLines.add(name + " " + name.toLowerCase() + "_tmp=null;");
			addRel.codeLines.add("if(" + name.toLowerCase() + ".isPresent()) " + name.toLowerCase() + "_tmp="
					+ name.toLowerCase() + ".get();\r\nelse\r\n");
			addRel.codeLines.add("throw new Exception(\"" + name + " not found\");\r\n");
			
			String getValFuncName=uniqueProperty.name.substring(0, 1).toUpperCase()+uniqueProperty.name.substring(1).toLowerCase();
			addRel.codeLines.add("TypedQuery<"+entity+"> "+entityObject+"_q2 = entityManager.createQuery(\"SELECT obj FROM "+entity+" obj where obj."+uniqueProperty.name+"='\"+"+entityObject+".get"+getValFuncName+"()+\"'\", "+entity+".class);");
			addRel.codeLines.add("List<"+entity+"> "+entityObject+"_rel= "+entityObject+"_q2.getResultList();");
			addRel.codeLines.add("if("+entityObject+"_rel!=null && "+entityObject+"_rel.size()>0) {");
			addRel.codeLines.add(entityObject+"_rel.get(0);");
			Property prop=classes.get(name).getProperty(getPlural(entityObject));
			if(prop==null)
				addRel.codeLines.add(name.toLowerCase()+"_tmp.set"+entity+"("+entityObject+"_rel.get(0));");
			else
				addRel.codeLines.add(name.toLowerCase()+"_tmp.get"+getPlural(entity)+"().add("+entityObject+"_rel.get(0));");
			prop=classes.get(entity).getProperty(getPlural(name.toLowerCase()));
			if(prop==null)
				addRel.codeLines.add(entityObject+"_rel.get(0).set"+name+"("+name.toLowerCase()+"_tmp);");//addRel.codeLines.add(name.toLowerCase()+"_tmp.set"+entity+"("+entityObject+"_rel.get(0));");
			else
				addRel.codeLines.add(entityObject+"_rel.get(0).get"+getPlural(name)+"().add("+name.toLowerCase()+"_tmp);");//name.toLowerCase()+"_tmp.get"+getPlural(entity)+"().add("+entityObject+"_rel.get(0));");
			addRel.codeLines.add(name.toLowerCase()+"Repo.save("+name.toLowerCase()+"_tmp);");
			addRel.codeLines.add("return "+entityObject+"_rel.get(0);");
			addRel.codeLines.add("}");
			addRel.codeLines.add("throw new Exception(\"" + entity + " not found\");\r\n");
		}
		return clazz;
	}
	
	public ClassMetaData addPutMapping(ClassMetaData clazz) {
		String name = clazz.getName().replace("Resource", "");
		String objectName = name.toLowerCase();
		String path = getPlural(objectName) + "/{id}";

		Property prop = clazz.addProperty("private ", name + "Repository", name.toLowerCase() + "Repo", false);
		prop.annotations.add("@Autowired");
		if (clazz.getFunction("update" + name) != null)
			return null;

		Function func = clazz.addFunction("public", name, "update" + name);
		func.exceptions.add("Exception");

		clazz.addImport("import org.springframework.web.bind.annotation.PutMapping;");
		System.out.println("'Path=\"" + path + "\"'");
		func.annotations.add("@PutMapping(path = \"/" + path + "\")");
		func.addParam("@PathVariable", "long", "id");
		func.addParam("@RequestBody", name, objectName);

		func.codeLines
				.add("Optional<" + name + "> " + name.toLowerCase() + "_persisted=" + name.toLowerCase() + "Repo.findById(id);");
		func.codeLines.add(name + " " + name.toLowerCase() + "_tmp=null;");
		func.codeLines.add("if(" + name.toLowerCase() + "_persisted.isPresent()) " + name.toLowerCase() + "_tmp="
				+ name.toLowerCase() + "_persisted.get();\r\nelse\r\n");
		func.codeLines.add("throw new Exception(\"" + name + " not found\");\r\n");
		List<Property> properties=classes.get(name).getProperties();
		for (Property property : properties) {
			if(property.primitive) {
				String propName=property.name;
				propName=propName.substring(0, 1).toUpperCase()+propName.substring(1);
				if(!"id".equalsIgnoreCase(propName))
					func.codeLines.add(name.toLowerCase() + "_tmp.set"+propName+"("+objectName + ".get"+propName+"()"+");");
			}
		}
		func.codeLines.add(objectName + "=" + objectName + "Repo.save(" +name.toLowerCase() + "_tmp);");
		func.codeLines.add(name.toLowerCase() + "_tmp.generateLinks();");
		func.codeLines.add("return "+name.toLowerCase() + "_tmp;");
		return clazz;
	}
	
	public ClassMetaData addPutRelMapping(ClassMetaData clazz, String entity) {		
		String name = clazz.getName().replace("Resource", "");
		ClassMetaData cmd=classes.get(entity);
		
		String objectName = name.toLowerCase();
		String path = getPlural(objectName) + "/{"+objectName+"Id}/"+getPlural(entity.toLowerCase())+"/{"+entity.toLowerCase()+"Id}";

		Property prop = clazz.addProperty("private ", entity + "Repository", entity.toLowerCase() + "Repo", false);
		prop.annotations.add("@Autowired");
		if (clazz.getFunction("add" + entity) != null)
			return null;

		Function func = clazz.addFunction("public", entity, "add" + entity);
		func.exceptions.add("Exception");

		clazz.addImport("import org.springframework.web.bind.annotation.PutMapping;");
		System.out.println("'Path=\"" + path + "\"'");
		func.annotations.add("@PutMapping(path = \"/" + path + "\")");
		func.addParam("@PathVariable", "long", objectName+"Id");
		func.addParam("@PathVariable", "long", entity.toLowerCase()+"Id");
		func.codeLines
				.add("Optional<" + name + "> " + name.toLowerCase() + "_persisted=" + name.toLowerCase() + "Repo.findById("+objectName+"Id);");
		func.codeLines.add(name + " " + name.toLowerCase() + "_tmp=null;");
		func.codeLines.add("if(" + name.toLowerCase() + "_persisted.isPresent()) " + name.toLowerCase() + "_tmp="
				+ name.toLowerCase() + "_persisted.get();\r\nelse\r\n");
		func.codeLines.add("throw new Exception(\"" + name + " not found\");\r\n");
		
		
		func.codeLines
				.add("Optional<" + entity + "> " + entity.toLowerCase() + "_persisted=" + entity.toLowerCase() + "Repo.findById("+entity.toLowerCase()+"Id);");
		func.codeLines.add(entity + " " + entity.toLowerCase() + "_tmp=null;");
		func.codeLines.add("if(" + entity.toLowerCase() + "_persisted.isPresent()) " + entity.toLowerCase() + "_tmp="
					+ entity.toLowerCase() + "_persisted.get();\r\nelse\r\n");
		func.codeLines.add("throw new Exception(\"" + entity + " not found\");\r\n");

		
		
		func.codeLines.add(name.toLowerCase() + "_tmp.get"+getPlural(entity)+"().add("+entity.toLowerCase() + "_tmp);");
		if(cmd.getProperty(getPlural(name.toLowerCase()))==null)
			func.codeLines.add(entity.toLowerCase() + "_tmp.set"+name+"("+name.toLowerCase() + "_tmp);");
		else
			func.codeLines.add(entity.toLowerCase() + "_tmp.get"+getPlural(name)+"().add("+name.toLowerCase() + "_tmp);");				
		
		func.codeLines.add(objectName + "Repo.save(" +name.toLowerCase() + "_tmp);");
		func.codeLines.add(entity.toLowerCase() + "Repo.save(" +entity.toLowerCase() + "_tmp);");
		
		func.codeLines.add(entity.toLowerCase() + "_tmp.generateLinks();");
		func.codeLines.add("return "+entity.toLowerCase() + "_tmp;");
		return addDeleteRelMapping(clazz,entity);
		//return clazz;
	}
	
	public ClassMetaData addDeleteRelMapping(ClassMetaData clazz, String entity) {		
		String name = clazz.getName().replace("Resource", "");
		ClassMetaData cmd=classes.get(entity);
		
		String objectName = name.toLowerCase();
		String path = getPlural(objectName) + "/{"+objectName+"Id}/"+getPlural(entity.toLowerCase())+"/{"+entity.toLowerCase()+"Id}";

		Property prop = clazz.addProperty("private ", entity + "Repository", entity.toLowerCase() + "Repo", false);
		prop.annotations.add("@Autowired");
		if (clazz.getFunction("remove" + entity) != null)
			return null;

		Function func = clazz.addFunction("public", entity, "remove" + entity);
		func.exceptions.add("Exception");

		clazz.addImport("import org.springframework.web.bind.annotation.DeleteMapping;");
		System.out.println("'Path=\"" + path + "\"'");
		func.annotations.add("@DeleteMapping(path = \"/" + path + "\")");
		func.addParam("@PathVariable", "long", objectName+"Id");
		func.addParam("@PathVariable", "long", entity.toLowerCase()+"Id");
		func.codeLines
				.add("Optional<" + name + "> " + name.toLowerCase() + "_persisted=" + name.toLowerCase() + "Repo.findById("+objectName+"Id);");
		func.codeLines.add(name + " " + name.toLowerCase() + "_tmp=null;");
		func.codeLines.add("if(" + name.toLowerCase() + "_persisted.isPresent()) " + name.toLowerCase() + "_tmp="
				+ name.toLowerCase() + "_persisted.get();\r\nelse\r\n");
		func.codeLines.add("throw new Exception(\"" + name + " not found\");\r\n");
		
		
		func.codeLines
				.add("Optional<" + entity + "> " + entity.toLowerCase() + "_persisted=" + entity.toLowerCase() + "Repo.findById("+entity.toLowerCase()+"Id);");
		func.codeLines.add(entity + " " + entity.toLowerCase() + "_tmp=null;");
		func.codeLines.add("if(" + entity.toLowerCase() + "_persisted.isPresent()) " + entity.toLowerCase() + "_tmp="
					+ entity.toLowerCase() + "_persisted.get();\r\nelse\r\n");
		func.codeLines.add("throw new Exception(\"" + entity + " not found\");\r\n");

		
		func.codeLines.add(name.toLowerCase() + "_tmp.get"+getPlural(entity)+"().remove("+entity.toLowerCase() + "_tmp);");
		if(cmd.getProperty(getPlural(name.toLowerCase()))==null)
			func.codeLines.add(entity.toLowerCase() + "_tmp.set"+name+"(null);");
		else
			func.codeLines.add(entity.toLowerCase() + "_tmp.get"+getPlural(name)+"().remove("+name.toLowerCase() + "_tmp);");				
		
		func.codeLines.add(objectName + "Repo.save(" +name.toLowerCase() + "_tmp);");
		func.codeLines.add(entity.toLowerCase() + "Repo.save(" +entity.toLowerCase() + "_tmp);");
		
		func.codeLines.add(entity.toLowerCase() + "_tmp.generateLinks();");
		func.codeLines.add("return "+entity.toLowerCase() + "_tmp;");

		return clazz;
	}
	

	public ClassMetaData addPostMapping(ClassMetaData clazz, String entity) {
		String name = clazz.getName().replace("Resource", "");
		String entityObject = entity.toLowerCase();
		String objectName = name.toLowerCase();
		String path = getPlural(objectName);
		String params = "";
		String paramArray[] = classes.get(entity).getRequiredNonPrimitiveProps();
		String clazzReqprops[] = classes.get(name).getRequiredNonPrimitiveProps();
		for (String prop : clazzReqprops) {
			// System.out.println(prop+"=="+entity);
			if (prop.contains(entity)) {
				System.out.println("Path not created: '" + path + "/{id}/" + entityObject + "'");
				return null;
			}
		}
		for (String param : paramArray) {
			if (!param.equals(name))
				params += "\"" + param.toLowerCase() + "Id\",";
		}
		if (params.endsWith(",")) {
			params += ",";
			params = params.replace(",,", "");
		}
		if (params.trim().length() > 0) {
			params = ",params= {" + params + "}";
		}

		Property prop = clazz.addProperty("private ", entity + "Repository", entity.toLowerCase() + "Repo", false);
		prop.annotations.add("@Autowired");
		if (clazz.getFunction("create" + entity) != null)
			return null;

		Function func = clazz.addFunction("public", entity, "create" + entity);
		func.exceptions.add("Exception");

		if (name.equals(entity)) {
			System.out.println("'Path=\"" + path + "\"" + params + "'");
			func.annotations.add("@PostMapping(path = \"/" + path + "\"" + params + ")");
			func.addParam("@RequestBody", entity, entityObject);
			if (params.trim().length() == 0) {
				func.codeLines.add(entityObject + "=" + entityObject + "Repo.save(" + entityObject + ");");
				func.codeLines.add(entityObject+".generateLinks();");
				func.codeLines.add("return "+entityObject+";");
			} else {
				if (paramArray != null) {
					for (String param : paramArray)
						if (!param.equals(name)) {
							prop = clazz.addProperty("private ", param + "Repository ", param.toLowerCase() + "Repo",
									false);
							prop.annotations.add("@Autowired");
							func.addParam("@RequestParam", "long", param.toLowerCase() + "Id");
							func.codeLines.add("Optional<" + param + "> " + param.toLowerCase() + "="
									+ param.toLowerCase() + "Repo.findById(" + param.toLowerCase() + "Id" + ");");
							func.codeLines.add(param + " " + param.toLowerCase() + "_tmp=null;");
							func.codeLines.add("if(" + param.toLowerCase() + ".isPresent()) " + param.toLowerCase()
									+ "_tmp=" + param.toLowerCase() + ".get();\r\nelse\r\n");
							func.codeLines.add("throw new Exception(\"" + param + " not found\");\r\n");
							func.codeLines.add(entityObject + ".set" + param + "(" + param.toLowerCase() + "_tmp);");
						}
					func.codeLines.add(entityObject + "=" + entityObject + "Repo.save(" + entityObject + ");");
					func.codeLines.add(entityObject+".generateLinks();");
					func.codeLines.add("return "+entityObject+";");
				}
			}
			// System.out.println("Class name:"+clazz.getName());
			// System.out.println(func.toString());
		} else {
			path += "/{id}/" + getPlural(entityObject);
			clazz=addPatchMapping(clazz, entity);
			System.out.println("'Path=\"/" + path + "\"" + params + "'");
			func.annotations.add("@PostMapping(path = \"/" + path + "\"" + params + ")");
			func.addParam("@RequestBody", entity, entityObject);
			func.addParam("@PathVariable", "long", "id");
			func.codeLines.add(
					"Optional<" + name + "> " + name.toLowerCase() + "=" + name.toLowerCase() + "Repo.findById(id);");
			func.codeLines.add(name + " " + name.toLowerCase() + "_tmp=null;");
			func.codeLines.add("if(" + name.toLowerCase() + ".isPresent()) " + name.toLowerCase() + "_tmp="
					+ name.toLowerCase() + ".get();\r\nelse\r\n");
			func.codeLines.add("throw new Exception(\"" + name + " not found\");\r\n");
			String mtmRel=getPlural((name.toLowerCase()));
			if(classes.get(entity).getProperty(mtmRel)!=null) {
				func.codeLines.add("List<"+name+"> "+getPlural(name.toLowerCase())+"="+entityObject+".get"+getPlural(name)+"();");
				func.codeLines.add("if("+getPlural(name.toLowerCase())+"==null)");
						func.codeLines.add(getPlural(name.toLowerCase())+"=new ArrayList<"+name+">();");
				func.codeLines.add(getPlural(name.toLowerCase())+".add("+name.toLowerCase()+"_tmp);");
				func.codeLines.add(entityObject+".set"+getPlural(name)+"("+getPlural(name.toLowerCase())+");");
			}else
				func.codeLines.add(entityObject + ".set" + name + "(" + name.toLowerCase() + "_tmp);");
			if (paramArray != null)
				for (String param : paramArray)
					if (!param.equals(name)) {
						prop = clazz.addProperty("private ", param + "Repository ", param.toLowerCase() + "Repo",
								false);
						clazz=addPatchMapping(clazz, param);
						prop.annotations.add("@Autowired");
						func.addParam("@RequestParam", "long", param.toLowerCase() + "Id");
						func.codeLines.add("Optional<" + param + "> " + param.toLowerCase() + "=" + param.toLowerCase()
								+ "Repo.findById(" + param.toLowerCase() + "Id" + ");");
						func.codeLines.add(param + " " + param.toLowerCase() + "_tmp=null;");
						func.codeLines.add("if(" + param.toLowerCase() + ".isPresent()) " + param.toLowerCase()
								+ "_tmp=" + param.toLowerCase() + ".get();\r\nelse\r\n");
						func.codeLines.add("throw new Exception(\"" + param + " not found\");\r\n");
						mtmRel=getPlural((param.toLowerCase()));
						if(classes.get(entity).getProperty(mtmRel)!=null) {
							func.codeLines.add("List<"+param+"> "+getPlural(param.toLowerCase())+"="+entityObject+".get"+getPlural(param)+"();");
							func.codeLines.add("if("+getPlural(param.toLowerCase())+"==null)");
									func.codeLines.add(getPlural(param.toLowerCase())+"=new ArrayList<"+param+">();");
							func.codeLines.add(getPlural(param.toLowerCase())+".add("+param.toLowerCase()+"_tmp);");
							func.codeLines.add(entityObject+".set"+getPlural(param)+"("+getPlural(param.toLowerCase())+");");
						}else {
							func.codeLines.add(entityObject + ".set" + param + "(" + param.toLowerCase() + "_tmp);");
						}
					}

			func.codeLines.add(entityObject + "=" + entityObject + "Repo.save(" + entityObject + ");");
			func.codeLines.add(entityObject+".generateLinks();");
			func.codeLines.add("return "+entityObject+";");
			// System.out.println(func.toString());
		}
		return clazz;
	}
	
	
	
	
	private void verifyAndResolveRelations(ClassMetaData entity, String rel) {
		ClassMetaData otm=classes.get(rel);
		Property otmProp=otm.getProperty(getPlural(entity.getName().toLowerCase()));
		if(otmProp==null)
			return;
		System.out.println("Fixing: "+entity.getName() +" ManyToMany "+rel);
		if(otmProp.removeAnnotation("@OneToMany")) {
			System.out.println("OneToMany removed from: "+rel);
			otmProp.annotations.add("@ManyToMany(mappedBy = \""+getPlural(otm.getName().toLowerCase())+"\")");
			otm.addImport("import javax.persistence.ManyToMany;");
			Property entityMtM=entity.getProperty(getPlural(rel.toLowerCase()));
			if(entityMtM.removeAnnotation("@OneToMany")){
				System.out.println("OneToOne Removed from: "+entity.getName());
				entityMtM.annotations.add("@ManyToMany(cascade = CascadeType.ALL)");
				String mtmRel="@JoinTable(name = \""+entity.getName().toLowerCase()+"_"+otm.getName().toLowerCase()+"\", \r\n" + 
						"      joinColumns = @JoinColumn(name = \""+entity.getName().toLowerCase()+"_id\", referencedColumnName = \"id\"), \r\n" + 
						"      inverseJoinColumns = @JoinColumn(name = \""+otm.getName().toLowerCase()+"_id\", \r\n" + 
						"      referencedColumnName = \"id\"))";
				entityMtM.annotations.add(mtmRel);
				entity.addImport("import javax.persistence.CascadeType;");
				entity.addImport("import javax.persistence.JoinColumn;");
				entity.addImport("import javax.persistence.JoinTable;");
				entity.addImport("import javax.persistence.ManyToMany;");
				entity.addImport("import javax.persistence.GeneratedValue;");
				entity.addImport("import javax.persistence.GenerationType;");
			}
		}
	}
	
	private void addHATEAOS(ClassMetaData entity) {
		entity.addImport("import org.springframework.hateoas.Link;");
		entity.addImport("import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;");
		List<Property> properties=entity.getProperties();
		String resource=entity.getName()+"Resource.class";
		Function func=entity.addFunction("public", "void", "generateLinks");
		func.codeLines.add("try{\r\n WebMvcLinkBuilder linkTo = null;");
		for (Property property : properties) {
			if(!property.primitive) {
				String name=property.type.replace("List<", "").replace(">", "");
				if(property.type.contains("List<")) {
					func.codeLines.add("linkTo=WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn("+resource+").get"+getPlural(name)+"(id));");
					func.codeLines.add("add(Link.of(linkTo.toUriComponentsBuilder().buildAndExpand(id).toString(),\""+getPlural(name.toLowerCase())+"\"));");
					//add(Link.of(linkTo.toUriComponentsBuilder().buildAndExpand(id).toString(),"timesheet-details"));
				}else {
					func.codeLines.add("linkTo = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn("+resource+").get"+(name)+"(id));");
					func.codeLines.add("add(Link.of(linkTo.toUriComponentsBuilder().buildAndExpand(id).toString(),\""+name.toLowerCase()+"\"));");
				}
			}
		}
		func.codeLines.add("linkTo = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn("+resource+").get"+getPlural(entity.getName())+"());");
		func.codeLines.add("add(Link.of(linkTo.toUriComponentsBuilder().path(\"/{id}\").buildAndExpand(id).toString(),\""+getPlural(entity.getName().toLowerCase())+"\"));");
		func.codeLines.add("}catch(Exception e){}");
	}

	public String generateClasses(String packageName, String folderPath, String htmlTemplate,String jsTemplate,String cssTemplate) throws Exception {
		// String packageName=basePackage.getText();
		if (packageName == null || packageName.trim().length() == 0) {
			return ("Please specify base package name like com.example.model");
		}
		String navigationButton="<a onclick='dynamic_table(\"%%ref%listUrl%\",\"%%ref%editUrl%\")'><button class='button'>%ref%</button></a>";
		String navigation="";
		Object keys[] = classes.keySet().toArray();
		for (Object key : keys) {
			ClassMetaData clazz = classes.get(key);
			if ("Entity".equals(clazz.getModelType()) && !clazz.getScope().equalsIgnoreCase("local")) {
				navigation+=navigationButton.replace("%ref%", clazz.getName());
				//clazz.exportTypeScript(folderPath, packageName);
				addHATEAOS(clazz);
				ClassMetaData resource = classes.get(clazz.getName() + "Resource");
				addGetMapping(resource, clazz.getName());
				addListGetMapping(resource, clazz.getName());
				addPostMapping(resource, clazz.getName());
				addPutMapping(resource);
				addDeleteMapping(resource);
				
				List<Property> properties = clazz.getProperties();
				if (properties != null && properties.size() > 0)
					for (Property prop : properties) {
						if (prop.primitive == false && !prop.type.replace("List<", "").replace(">", "")
								.equals(resource.getName().replace("Resource", ""))) {
							if (prop.type.startsWith("List<")) {
								verifyAndResolveRelations(clazz, prop.type.replace("List<", "").replace(">", ""));
								addPutRelMapping(resource, prop.type.replace("List<", "").replace(">", ""));
								addListGetMapping(resource, prop.type.replace("List<", "").replace(">", ""));
							}
							else
								addGetMapping(resource, prop.type);
							addPostMapping(resource, prop.type.replace("List<", "").replace(">", ""));
						}
					}
			}
		}
		
		for (Object key : keys) {
			System.out.println("key:"+key);
			ClassMetaData clazz = classes.get(key);
			if("api".equalsIgnoreCase(clazz.getModelType())) {
				ClassMetaData resource=classes.get(clazz.getName()+"Resource");
				API api=clazz.getAPI();
				Set<String> imports=api.getImportList();
				if(imports!=null)
				for (String imprt : imports) {
					resource.addImport(imprt);
				}
				
				//resource.addProperty(access, type, name, primitive)
				ClassMetaData payload=classes.get(clazz.getName()+".Payload");
				String requestBody="@RequestBody(required = false)";
				if(payload.getProperty("requestpayload").required)
					requestBody="@RequestBody(required = true)";
				
				Function apiActionImpl=resource.addFunction("public", "ResponsePayload", clazz.getName().toLowerCase());
				
				
				apiActionImpl.addParam(requestBody,"RequestPayload" , "requestPayload");
				String method=api.method.toLowerCase();
				method=method.substring(0, 1).toUpperCase()+method.substring(1);
				List<Property> queryStringProps= clazz.getProperties();
				ClassMetaData requestheaders=classes.get(clazz.getRoot()+".Headers"+"."+"RequestHeaders");
				
				List<Property> properties=requestheaders.getProperties();
				if(properties!=null && properties.size()>0) {
					resource.addImport("import org.springframework.web.bind.annotation.RequestHeader;");
					requestheaders.addAnnotation("@AllArgsConstructor");
					requestheaders.addImport("import lombok.AllArgsConstructor;");
					requestheaders.addAnnotation("@NoArgsConstructor");
					requestheaders.addImport("import lombok.NoArgsConstructor;");
					for (Property property : properties) {
						apiActionImpl.addParam("@RequestHeader(\""+property.name+"\")", property.type, property.name);
					}
				}
				//@PostMapping(path = "/entrys",params= {"projectId","timesheetId"})
				//@RequestParam long timesheetId
				//@PathVariable long id
				String params="";
				
				api.path=clazz.getProperty("path").value;
				for (Property property : queryStringProps) {
					if(property.primitive && !property.name.equalsIgnoreCase("path")) {
						if(!api.path.contains("{"+property.name+"}")) {
							apiActionImpl.addParam("@RequestParam", property.type, property.name);
							params+="\""+property.name+"\",";
						}else {
							apiActionImpl.addParam("@PathVariable", property.type, property.name);
						}
					}
				}
				if(params.trim().length()>0)
					params=(params+",").replace(",,", "");
				//@RequestHeader("category") String category
				//api.addQueryParam(name, type);
				apiActionImpl.annotations.add("@"+method+"Mapping(path=\""+api.path+"\", params={"+params+"})");
				apiActionImpl.codeLines.add(new String(Base64.getDecoder().decode(api.code)));
				resource.setCustomCode(new String(Base64.getDecoder().decode(api.custom)));
				clazz.exportAPI(folderPath, packageName,classes);
			}	
		}
		System.out.println("navigation: "+navigation);
		for (Object key : keys) {
			ClassMetaData clazz = classes.get(key);
			clazz.exportJava(folderPath, packageName);
			navigation=clazz.exportGUI(folderPath, navigation);
		}
		System.out.println("navigation-updated: "+navigation);
		String dirPath=folderPath.split("java")[0]+"resources/static/admin-ui/";
		String data = htmlTemplate.replace("%navigation%", navigation);
		File file=new File(dirPath);
		if(!file.exists() || !file.isDirectory()) {
			file.mkdir();
		}
		FileOutputStream fos = new FileOutputStream(
				new File(dirPath+"/index.html"));
		fos.write(data.getBytes());
		fos.flush();
		fos.close();
		
		fos = new FileOutputStream(
				new File(dirPath+"/DynamicTableJs.js"));
		fos.write(jsTemplate.getBytes());
		fos.flush();
		fos.close();
		
		fos = new FileOutputStream(
				new File(dirPath+"/DynamicTableCss.css"));
		fos.write(cssTemplate.getBytes());
		fos.flush();
		fos.close();
		
		// System.out.println(data);
		classes.clear();
		return null;
	}
	
	private void addCompositeKey() {
//		@Table( name="Employee",
//			    uniqueConstraints=
//			        @UniqueConstraint(columnNames={"column_1", "column_2"},name="")
//			)
	}

}
