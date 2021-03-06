package com.github.moksh.generator.core;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsonschema2pojo.util.Inflector;

import com.github.moksh.generator.GUI.PromptDialogBox;
import com.github.moksh.generator.GUI.Utils.CommonUtils;
import com.github.moksh.generator.core.ClassMetaData.API;
import com.github.moksh.generator.core.ClassMetaData.Function;
import com.github.moksh.generator.core.ClassMetaData.Property;

public class CodeGen {
	// public static CodeGen CG = new CodeGen();
	public Map<String, ClassMetaData> classes;
	// public static final String LOCATION = "D:\\nvme\\data\\CodeGen";

	private CodeGen() {
		classes = new HashMap<String, ClassMetaData>();
	}

	public static CodeGen getInstance() {
		return new CodeGen();
	}

	public ClassMetaData createEntityClass(String name, String scope, String root) {
		if (scope != null && scope.trim().length() > 0)
			scope = scope.toLowerCase().trim();
		if (!scope.equals("local"))
			root = name;
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
			// prop.annotations.add("@JsonIgnore");
			prop.annotations.add("@GeneratedValue(strategy = GenerationType.AUTO)");
			prop.required = false;
			clazz.addImport("import javax.persistence.Entity;");
			clazz.addImport("import javax.persistence.GeneratedValue;");
			clazz.addImport("import javax.persistence.GenerationType;");
			clazz.addImport("import javax.persistence.Id;");
			clazz.addImport("import javax.validation.constraints.NotEmpty;");
			clazz.addImport("import com.fasterxml.jackson.annotation.JsonIgnore;");
			clazz.addImport("import org.springframework.hateoas.EntityModel;");
			clazz.addExtends("EntityModel<" + clazz.getName() + ">");
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
		ClassMetaData clazzResource = createResourceClass(name);
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

	public ClassMetaData addListGetMapping(ClassMetaData clazz, String entity, String entityPropertyName) {
		String name = clazz.getName().replace("Resource", "");
		Property prop = clazz.addProperty("private ", entity + "Repository", CommonUtils.toLowerFirst(entity) + "Repo",
				false);
		prop.annotations.add("@Autowired");
		Function func = clazz.addFunction("public", "List<" + entity + ">",
				"get" + CommonUtils.toUpperFirst(entityPropertyName));

		String path = CommonUtils.getSingular(CommonUtils.toLowerFirst(name));
		if (name.equals(entity)) {
			func.annotations.add("@GetMapping(path = \"/" + path + "\")");
			func.codeLines.add("List<" + entity + "> fetched=" + "(List<" + entity + ">)" + prop.name + ".findAll();");
			func.codeLines.add("fetched.forEach(obj-> obj.generateLinks());");
			func.codeLines.add("if(fetched.size()==0) fetched.add(new " + entity + "());");
			func.codeLines.add("return fetched;");
		} else {
			path = path + "/{id}/" + CommonUtils.getSingular(entityPropertyName);
			func.annotations.add("@GetMapping(path = \"/" + path + "\")");
			func.addParam("@PathVariable", "long", "id");
			func.exceptions.add("Exception");
			func.codeLines.add("Optional<" + name + "> " + CommonUtils.toLowerFirst(name) + "="
					+ CommonUtils.toLowerFirst(name) + "Repo.findById(id);");
			func.codeLines.add("if(" + CommonUtils.toLowerFirst(name) + ".isPresent()){ ");
			func.codeLines.add("List<" + entity + "> " + CommonUtils.getPlural(CommonUtils.toLowerFirst(entity)) + "="
					+ CommonUtils.toLowerFirst(name) + ".get().get" + CommonUtils.getPlural(entity) + "();");
			func.codeLines.add("if(" + CommonUtils.getPlural(CommonUtils.toLowerFirst(entity)) + "==null || "
					+ CommonUtils.getPlural(CommonUtils.toLowerFirst(entity)) + ".size()==0){");
			func.codeLines.add(CommonUtils.getPlural(CommonUtils.toLowerFirst(entity)) + "=new ArrayList<" + entity
					+ ">();" + CommonUtils.getPlural(CommonUtils.toLowerFirst(entity)) + ".add(new " + entity
					+ "());}else");
			func.codeLines.add(CommonUtils.getPlural(CommonUtils.toLowerFirst(entity))
					+ ".forEach(obj-> obj.generateLinks());\r\n");
			func.codeLines.add("return " + CommonUtils.getPlural(CommonUtils.toLowerFirst(entity)) + ";\r\n}");
			// func.codeLines.add("return "+CommonUtils.toLowerFirst(entity)+";\r\n}");
			func.codeLines.add("throw new Exception(\"" + name + " not found\");");
		}
		return clazz;
	}

	public ClassMetaData addGetMappingForUniqueProperty(ClassMetaData resource) {
		String entity = resource.getName().replace("Resource", "");
		ClassMetaData entityClass = classes.get(entity);
		String entityLowerFirst = CommonUtils.toLowerFirst(entity);
		List<Property> entityProperties = entityClass.getProperties();
		Property uniqueProperty = null;
		for (Property property : entityProperties) {
			Set<String> annots = property.annotations;
			for (String annot : annots) {
				if (annot.contains("@Column(unique = true") && property.primitive) {
					uniqueProperty = property;
					break;
				}
			}
			if (uniqueProperty != null)
				break;
		}
		if (uniqueProperty != null) {
			String path = entityLowerFirst + "/{" + uniqueProperty.name + "}";
			Property property = resource.addProperty("private", "EntityManager", "entityManager", false);
			property.annotations.add("@PersistenceContext");
			resource.addImport("import javax.persistence.EntityManager;");
			resource.addImport("import javax.persistence.PersistenceContext;");
			resource.addImport("import org.springframework.web.bind.annotation.GetMapping;");
			resource.addImport("import org.springframework.web.bind.annotation.RequestParam;");
			resource.addImport("import javax.persistence.TypedQuery;");
			Function getMapping = resource.addFunction("public", entity, "get" + entity + "By" + uniqueProperty.name);
			getMapping.exceptions.add("Exception");
			getMapping.annotations
					.add("@GetMapping(path = \"/" + path + "\",params={\"" + uniqueProperty.name + "\"})");
			getMapping.addParam("@RequestParam", CommonUtils.toUpperFirst(uniqueProperty.type), uniqueProperty.name);
			// String
			// getValFuncName=CommonUtils.toUpperFirst(uniqueProperty.name);//.substring(0,
			// 1).toUpperCase()+uniqueProperty.name.substring(1).toLowerCase();
			getMapping.codeLines.add("TypedQuery<" + entity + "> " + entityLowerFirst
					+ "_tq = entityManager.createQuery(\"SELECT obj FROM " + entity + " obj where obj."
					+ uniqueProperty.name + "='\"+" + uniqueProperty.name + "+\"'\", " + entity + ".class);");
			getMapping.codeLines.add(
					"List<" + entity + "> " + entityLowerFirst + "List = " + entityLowerFirst + "_tq.getResultList();");
			getMapping.codeLines
					.add("if(" + entityLowerFirst + "List!=null && " + entityLowerFirst + "List.size()>0) {");
			getMapping.codeLines.add(entity + " fetched=" + entityLowerFirst + "List.get(0);");
			getMapping.codeLines.add("fetched.generateLinks();");
			getMapping.codeLines.add("return fetched;");
			getMapping.codeLines.add("}");
			getMapping.codeLines.add("throw new Exception(\"" + entity + " not found\");");
		}
		return entityClass;
	}

	public ClassMetaData addGetMapping(ClassMetaData clazz, String entity, String entityPropertyName) {
		String name = clazz.getName().replace("Resource", "");
		Property prop = clazz.addProperty("private ", entity + "Repository", CommonUtils.toLowerFirst(entity) + "Repo",
				false);
		prop.annotations.add("@Autowired");
		Function func = clazz.addFunction("public", entity, "get" + CommonUtils.toUpperFirst(entityPropertyName));
		String path = CommonUtils.getPlural(CommonUtils.toLowerFirst(name)) + "/{id}";
		func.addParam("@PathVariable", "long", "id");
		if (name.equals(entity)) {
			func.annotations.add("@GetMapping(path = \"/" + path + "\")");
			func.exceptions.add("Exception");
			func.codeLines.add("Optional<" + entity + "> " + CommonUtils.toLowerFirst(entity) + "=" + prop.name
					+ ".findById(id);");
			func.codeLines.add("if(!" + CommonUtils.toLowerFirst(entity) + ".isPresent())");
			func.codeLines.add("throw new Exception(\"" + entity + " not found\");");
			func.codeLines.add(entity + " fetched=" + CommonUtils.toLowerFirst(entity) + ".get();");
			func.codeLines.add("fetched.generateLinks();");
			func.codeLines.add("return fetched;");
		} else {
			func.annotations.add("@GetMapping(path = \"/" + path + "/" + entityPropertyName + "\")");
			func.exceptions.add("Exception");
			func.codeLines.add("Optional<" + name + "> " + CommonUtils.toLowerFirst(name) + "="
					+ CommonUtils.toLowerFirst(name) + "Repo.findById(id);");
			func.codeLines.add("if(" + CommonUtils.toLowerFirst(name) + ".isPresent()){ ");
			func.codeLines
					.add(entity + " " + CommonUtils.toLowerFirst(entity) + "_tmp=" + CommonUtils.toLowerFirst(name)
							+ ".get().get" + CommonUtils.toUpperFirst(entityPropertyName) + "();");
			func.codeLines.add("if(" + CommonUtils.toLowerFirst(entity) + "_tmp==null)");
			func.codeLines.add(CommonUtils.toLowerFirst(entity) + "_tmp=new " + entity + "();");
			func.codeLines.add(CommonUtils.toLowerFirst(entity) + "_tmp.generateLinks();\r\n");
			func.codeLines.add("return " + CommonUtils.toLowerFirst(entity) + "_tmp;\r\n}");
			func.codeLines.add("throw new Exception(\"" + name + " not found\");");
		}
		return clazz;
	}

	public ClassMetaData addDeleteMapping(ClassMetaData clazz) {
		String name = clazz.getName().replace("Resource", "");
		String objectName = CommonUtils.toLowerFirst(name);
		String path = CommonUtils.getSingular(objectName) + "/{id}";
		Property prop = clazz.addProperty("private ", name + "Repository", CommonUtils.toLowerFirst(name) + "Repo",
				false);
		prop.annotations.add("@Autowired");
		if (clazz.getFunction("delete" + name) != null)
			return null;

		Function func = clazz.addFunction("public", "void", "detele" + name);
		func.exceptions.add("Exception");

		clazz.addImport("import org.springframework.web.bind.annotation.DeleteMapping;");
		func.annotations.add("@DeleteMapping(path = \"/" + path + "\")");
		func.addParam("@PathVariable", "long", "id");
		func.exceptions.add("Exception");
		func.codeLines.add("Optional<" + name + "> " + CommonUtils.toLowerFirst(name) + "="
				+ CommonUtils.toLowerFirst(name) + "Repo.findById(id);");
		func.codeLines.add("if(" + CommonUtils.toLowerFirst(name) + ".isPresent()) " + CommonUtils.toLowerFirst(name)
				+ ".get(); else");
		func.codeLines.add("throw new Exception(\"" + name + " not found\");");
		func.codeLines.add(CommonUtils.toLowerFirst(name) + "Repo.deleteById(id);");
		return clazz;
	}

	public ClassMetaData addPatchMapping(ClassMetaData clazz, String entity, String entityPropertyName) {
		String name = clazz.getName().replace("Resource", "");
		// String entityPropertyName = CommonUtils.toLowerFirst(entity);
		String objectName = CommonUtils.toLowerFirst(name);
		String path = CommonUtils.getSingular(objectName);
		path += "/{id}/" + CommonUtils.getSingular(entityPropertyName);
		// System.out.println("'Path=\"/" + path + "\"" + params + "'");
		ClassMetaData entityClass = classes.get(entity);
		List<Property> entityProperties = entityClass.getProperties();
		Property uniqueProperty = null;
		for (Property property : entityProperties) {
			Set<String> annots = property.annotations;
			for (String annot : annots) {
				if (annot.contains("@Column(unique = true")) {
					uniqueProperty = property;
					break;
				}
			}
			if (uniqueProperty != null)
				break;
		}
		if (uniqueProperty != null) {

			Property property = clazz.addProperty("private", "EntityManager", "entityManager", false);
			property.annotations.add("@PersistenceContext");
			clazz.addImport("import javax.persistence.EntityManager;");
			clazz.addImport("import javax.persistence.PersistenceContext;");
			clazz.addImport("import org.springframework.web.bind.annotation.PatchMapping;");
			clazz.addImport("import javax.persistence.TypedQuery;");
			Function addRel = clazz.addFunction("public", entity,
					"addRelTo" + CommonUtils.toUpperFirst(CommonUtils.getSingular(entityPropertyName)));
			addRel.exceptions.add("Exception");
			addRel.annotations.add("@PatchMapping(path = \"/" + path + "\")");
			addRel.addParam("@RequestBody", entity, entityPropertyName);
			addRel.addParam("@PathVariable", "long", "id");
			addRel.codeLines.add("Optional<" + name + "> " + CommonUtils.toLowerFirst(name) + "="
					+ CommonUtils.toLowerFirst(name) + "Repo.findById(id);");
			addRel.codeLines.add(name + " " + CommonUtils.toLowerFirst(name) + "_tmp=null;");
			addRel.codeLines
					.add("if(" + CommonUtils.toLowerFirst(name) + ".isPresent()) " + CommonUtils.toLowerFirst(name)
							+ "_tmp=" + CommonUtils.toLowerFirst(name) + ".get();\r\nelse\r\n");
			addRel.codeLines.add("throw new Exception(\"" + name + " not found\");\r\n");

			String getValFuncName = CommonUtils.toUpperFirst(uniqueProperty.name);// .substring(0,
																					// 1).toUpperCase()+uniqueProperty.name.substring(1).toLowerCase();
			addRel.codeLines.add("TypedQuery<" + entity + "> " + entityPropertyName
					+ "_q2 = entityManager.createQuery(\"SELECT obj FROM " + entity + " obj where obj."
					+ uniqueProperty.name + "='\"+" + entityPropertyName + CommonUtils.resolveGetter(uniqueProperty)
					+ getValFuncName + "()+\"'\", " + entity + ".class);");
			addRel.codeLines.add("List<" + entity + "> " + entityPropertyName + "_rel= " + entityPropertyName
					+ "_q2.getResultList();");
			addRel.codeLines
					.add("if(" + entityPropertyName + "_rel!=null && " + entityPropertyName + "_rel.size()>0) {");
			addRel.codeLines.add(entityPropertyName + "_rel.get(0);");
			Property prop = classes.get(name).getProperty(CommonUtils.getPlural(entityPropertyName));
			if (prop == null)
				addRel.codeLines.add(CommonUtils.toLowerFirst(name) + "_tmp.set" + entity + "(" + entityPropertyName
						+ "_rel.get(0));");
			else
				addRel.codeLines.add(CommonUtils.toLowerFirst(name) + "_tmp.get" + CommonUtils.getPlural(entity)
						+ "().add(" + entityPropertyName + "_rel.get(0));");
			prop = classes.get(entity).getProperty(CommonUtils.getPlural(CommonUtils.toLowerFirst(name)));
			if (prop == null)
				addRel.codeLines.add(entityPropertyName + "_rel.get(0).set" + name + "("
						+ CommonUtils.toLowerFirst(name) + "_tmp);");// addRel.codeLines.add(CommonUtils.toLowerFirst(name)+"_tmp.set"+entity+"("+entityObject+"_rel.get(0));");
			else
				addRel.codeLines.add(entityPropertyName + "_rel.get(0).get" + CommonUtils.getPlural(name) + "().add("
						+ CommonUtils.toLowerFirst(name) + "_tmp);");// CommonUtils.toLowerFirst(name)+"_tmp.get"+CommonUtils.getPlural(entity)+"().add("+entityObject+"_rel.get(0));");
			addRel.codeLines
					.add(CommonUtils.toLowerFirst(name) + "Repo.save(" + CommonUtils.toLowerFirst(name) + "_tmp);");
			addRel.codeLines.add("return " + entityPropertyName + "_rel.get(0);");
			addRel.codeLines.add("}");
			addRel.codeLines.add("throw new Exception(\"" + entity + " not found\");\r\n");
		}
		return clazz;
	}

	public ClassMetaData addPutMapping(ClassMetaData clazz) {
		String name = clazz.getName().replace("Resource", "");
		String objectName = CommonUtils.toLowerFirst(name);
		String path = CommonUtils.getSingular(objectName) + "/{id}";

		Property prop = clazz.addProperty("private ", name + "Repository", CommonUtils.toLowerFirst(name) + "Repo",
				false);
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

		func.codeLines.add("Optional<" + name + "> " + CommonUtils.toLowerFirst(name) + "_persisted="
				+ CommonUtils.toLowerFirst(name) + "Repo.findById(id);");
		func.codeLines.add(name + " " + CommonUtils.toLowerFirst(name) + "_tmp=null;");
		func.codeLines.add(
				"if(" + CommonUtils.toLowerFirst(name) + "_persisted.isPresent()) " + CommonUtils.toLowerFirst(name)
						+ "_tmp=" + CommonUtils.toLowerFirst(name) + "_persisted.get();\r\nelse\r\n");
		func.codeLines.add("throw new Exception(\"" + name + " not found\");\r\n");
		List<Property> properties = classes.get(name).getProperties();
		for (Property property : properties) {
			if (property.primitive) {
				String propName = property.name;
				propName = propName.substring(0, 1).toUpperCase() + propName.substring(1);
				if (!"id".equalsIgnoreCase(propName))
					func.codeLines.add(CommonUtils.toLowerFirst(name) + "_tmp.set" + propName + "(" + objectName
							+ CommonUtils.resolveGetter(property) + propName + "()" + ");");
			}
		}
		func.codeLines.add(objectName + "=" + objectName + "Repo.save(" + CommonUtils.toLowerFirst(name) + "_tmp);");
		func.codeLines.add(CommonUtils.toLowerFirst(name) + "_tmp.generateLinks();");
		func.codeLines.add("return " + CommonUtils.toLowerFirst(name) + "_tmp;");
		return clazz;
	}

	public ClassMetaData addPutRelMapping(ClassMetaData clazz, String entity, String entityPropertyName) {
		String name = clazz.getName().replace("Resource", "");
		System.out.println("Put relation in " + name + " of " + entity);
		ClassMetaData cmd = classes.get(entity);

		String objectName = CommonUtils.toLowerFirst(name);
		String path = CommonUtils.getSingular(objectName) + "/{" + objectName + "Id}/"
				+ CommonUtils.getSingular(entityPropertyName) + "/{" + CommonUtils.getSingular(entityPropertyName)
				+ "Id}";

		Property prop = clazz.addProperty("private ", entity + "Repository", CommonUtils.toLowerFirst(entity) + "Repo",
				false);
		prop.annotations.add("@Autowired");
		if (clazz.getFunction("add" + CommonUtils.toUpperFirst(CommonUtils.getSingular(entityPropertyName))) != null)
			return null;

		Function func = clazz.addFunction("public", entity,
				"add" + CommonUtils.toUpperFirst(CommonUtils.getSingular(entityPropertyName)));
		func.exceptions.add("Exception");

		clazz.addImport("import org.springframework.web.bind.annotation.PutMapping;");
		System.out.println("'Path=\"" + path + "\"'");
		func.annotations.add("@PutMapping(path = \"/" + path + "\")");
		func.addParam("@PathVariable", "long", objectName + "Id");
		func.addParam("@PathVariable", "long", CommonUtils.getSingular(entityPropertyName) + "Id");
		func.codeLines.add("Optional<" + name + "> " + CommonUtils.toLowerFirst(name) + "_persisted="
				+ CommonUtils.toLowerFirst(name) + "Repo.findById(" + objectName + "Id);");
		func.codeLines.add(name + " " + CommonUtils.toLowerFirst(name) + "_tmp=null;");
		func.codeLines.add(
				"if(" + CommonUtils.toLowerFirst(name) + "_persisted.isPresent()) " + CommonUtils.toLowerFirst(name)
						+ "_tmp=" + CommonUtils.toLowerFirst(name) + "_persisted.get();\r\nelse\r\n");
		func.codeLines.add("throw new Exception(\"" + name + " not found\");\r\n");

		func.codeLines.add("Optional<" + entity + "> " + CommonUtils.getSingular(entityPropertyName) + "_persisted="
				+ CommonUtils.toLowerFirst(entity) + "Repo.findById(" + CommonUtils.getSingular(entityPropertyName)
				+ "Id);");
		func.codeLines.add(entity + " " + CommonUtils.getSingular(entityPropertyName) + "_tmp=null;");
		func.codeLines.add("if(" + CommonUtils.getSingular(entityPropertyName) + "_persisted.isPresent()) "
				+ CommonUtils.getSingular(entityPropertyName) + "_tmp=" + CommonUtils.getSingular(entityPropertyName)
				+ "_persisted.get();\r\nelse\r\n");
		func.codeLines.add("throw new Exception(\"" + entity + " not found\");\r\n");

		func.codeLines.add(CommonUtils.toLowerFirst(name) + "_tmp.get" + CommonUtils.toUpperFirst(entityPropertyName)
				+ "().add(" + CommonUtils.getSingular(entityPropertyName) + "_tmp);");
		if (cmd.getProperty(CommonUtils.getPlural(CommonUtils.toLowerFirst(name))) == null)
			func.codeLines.add(CommonUtils.getSingular(entityPropertyName) + "_tmp.set" + name + "("
					+ CommonUtils.toLowerFirst(name) + "_tmp);");
		else
			func.codeLines.add(CommonUtils.getSingular(entityPropertyName) + "_tmp.get" + CommonUtils.getPlural(name)
					+ "().add(" + CommonUtils.toLowerFirst(name) + "_tmp);");

		func.codeLines.add(objectName + "Repo.save(" + CommonUtils.toLowerFirst(name) + "_tmp);");
		func.codeLines.add(CommonUtils.toLowerFirst(entity) + "Repo.save(" + CommonUtils.getSingular(entityPropertyName)
				+ "_tmp);");

		func.codeLines.add(CommonUtils.getSingular(entityPropertyName) + "_tmp.generateLinks();");
		func.codeLines.add("return " + CommonUtils.getSingular(entityPropertyName) + "_tmp;");
		return addDeleteRelMapping(clazz, entity, entityPropertyName);
		// return clazz;
	}

	public ClassMetaData addDeleteRelMapping(ClassMetaData clazz, String entity, String entityPropertyName) {
		String name = clazz.getName().replace("Resource", "");
		ClassMetaData cmd = classes.get(entity);

		String objectName = CommonUtils.toLowerFirst(name);
		String path = CommonUtils.getSingular(objectName) + "/{" + objectName + "Id}/" +  CommonUtils.getSingular(entityPropertyName) + "/{"
				+ CommonUtils.getSingular(entityPropertyName) + "Id}";

		Property prop = clazz.addProperty("private ", entity + "Repository", CommonUtils.toLowerFirst(entity) + "Repo",
				false);
		prop.annotations.add("@Autowired");
		if (clazz.getFunction("remove" + CommonUtils.toUpperFirst(CommonUtils.getSingular(entityPropertyName))) != null)
			return null;

		Function func = clazz.addFunction("public", entity,
				"remove" + CommonUtils.toUpperFirst(CommonUtils.getSingular(entityPropertyName)));
		func.exceptions.add("Exception");

		clazz.addImport("import org.springframework.web.bind.annotation.DeleteMapping;");
		System.out.println("'Path=\"" + path + "\"'");
		func.annotations.add("@DeleteMapping(path = \"/" + path + "\")");
		func.addParam("@PathVariable", "long", objectName + "Id");
		func.addParam("@PathVariable", "long", CommonUtils.getSingular(entityPropertyName) + "Id");
		func.codeLines.add("Optional<" + name + "> " + CommonUtils.toLowerFirst(name) + "_persisted="
				+ CommonUtils.toLowerFirst(name) + "Repo.findById(" + objectName + "Id);");
		func.codeLines.add(name + " " + CommonUtils.toLowerFirst(name) + "_tmp=null;");
		func.codeLines.add(
				"if(" + CommonUtils.toLowerFirst(name) + "_persisted.isPresent()) " + CommonUtils.toLowerFirst(name)
						+ "_tmp=" + CommonUtils.toLowerFirst(name) + "_persisted.get();\r\nelse\r\n");
		func.codeLines.add("throw new Exception(\"" + name + " not found\");\r\n");

		func.codeLines.add("Optional<" + entity + "> " + CommonUtils.getSingular(entityPropertyName) + "_persisted="
				+ CommonUtils.toLowerFirst(entity) + "Repo.findById(" + CommonUtils.getSingular(entityPropertyName)
				+ "Id);");
		func.codeLines.add(entity + " " + CommonUtils.getSingular(entityPropertyName) + "_tmp=null;");
		func.codeLines.add("if(" + CommonUtils.getSingular(entityPropertyName) + "_persisted.isPresent()) "
				+ CommonUtils.getSingular(entityPropertyName) + "_tmp=" + CommonUtils.getSingular(entityPropertyName)
				+ "_persisted.get();\r\nelse\r\n");
		func.codeLines.add("throw new Exception(\"" + entity + " not found\");\r\n");

		func.codeLines.add(CommonUtils.toLowerFirst(name) + "_tmp.get" + CommonUtils.toUpperFirst(entityPropertyName)
				+ "().remove(" + CommonUtils.getSingular(entityPropertyName) + "_tmp);");
		if (cmd.getProperty(CommonUtils.getPlural(CommonUtils.toLowerFirst(name))) == null)
			func.codeLines.add(CommonUtils.getSingular(entityPropertyName) + "_tmp.set" + name + "(null);");
		else
			func.codeLines.add(CommonUtils.getSingular(entityPropertyName) + "_tmp.get" + CommonUtils.getPlural(name)
					+ "().remove(" + CommonUtils.getSingular(entityPropertyName) + "_tmp);");

		func.codeLines.add(objectName + "Repo.save(" + CommonUtils.toLowerFirst(name) + "_tmp);");
		func.codeLines.add(CommonUtils.toLowerFirst(entity) + "Repo.save(" + CommonUtils.getSingular(entityPropertyName)
				+ "_tmp);");

		func.codeLines.add(CommonUtils.getSingular(entityPropertyName) + "_tmp.generateLinks();");
		func.codeLines.add("return " + CommonUtils.getSingular(entityPropertyName) + "_tmp;");

		return clazz;
	}

	public ClassMetaData addPostMapping(ClassMetaData clazz, String entity) {
		String name = clazz.getName().replace("Resource", "");
		String entityObject = CommonUtils.toLowerFirst(entity);
		String objectName = CommonUtils.toLowerFirst(name);
		String path = CommonUtils.getSingular(objectName);
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
				params += "\"" + CommonUtils.toLowerFirst(param) + "Id\",";
		}
		if (params.endsWith(",")) {
			params += ",";
			params = params.replace(",,", "");
		}
		if (params.trim().length() > 0) {
			params = ",params= {" + params + "}";
		}

		Property prop = clazz.addProperty("private ", entity + "Repository", CommonUtils.toLowerFirst(entity) + "Repo",
				false);
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
				func.codeLines.add(entityObject + ".generateLinks();");
				func.codeLines.add("return " + entityObject + ";");
			} else {
				if (paramArray != null) {
					for (String param : paramArray)
						if (!param.equals(name)) {
							prop = clazz.addProperty("private ", param + "Repository ",
									CommonUtils.toLowerFirst(param) + "Repo", false);
							prop.annotations.add("@Autowired");
							func.addParam("@RequestParam", "long", CommonUtils.toLowerFirst(param) + "Id");
							func.codeLines.add("Optional<" + param + "> " + CommonUtils.toLowerFirst(param) + "="
									+ CommonUtils.toLowerFirst(param) + "Repo.findById("
									+ CommonUtils.toLowerFirst(param) + "Id" + ");");
							func.codeLines.add(param + " " + CommonUtils.toLowerFirst(param) + "_tmp=null;");
							func.codeLines.add("if(" + CommonUtils.toLowerFirst(param) + ".isPresent()) "
									+ CommonUtils.toLowerFirst(param) + "_tmp=" + CommonUtils.toLowerFirst(param)
									+ ".get();\r\nelse\r\n");
							func.codeLines.add("throw new Exception(\"" + param + " not found\");\r\n");
							func.codeLines.add(
									entityObject + ".set" + param + "(" + CommonUtils.toLowerFirst(param) + "_tmp);");
						}
					func.codeLines.add(entityObject + "=" + entityObject + "Repo.save(" + entityObject + ");");
					func.codeLines.add(entityObject + ".generateLinks();");
					func.codeLines.add("return " + entityObject + ";");
				}
			}
			// System.out.println("Class name:"+clazz.getName());
			// System.out.println(func.toString());
		} else {
			path += "/{id}/" + CommonUtils.getPlural(entityObject);
			System.out.println("---------------------------" + clazz.getName() + " and " + entity);
			clazz = addPatchMapping(clazz, entity, CommonUtils.toLowerFirst(entity));
			System.out.println("'Path=\"/" + path + "\"" + params + "'");
			func.annotations.add("@PostMapping(path = \"/" + path + "\"" + params + ")");
			func.addParam("@RequestBody", entity, entityObject);
			func.addParam("@PathVariable", "long", "id");
			func.codeLines.add("Optional<" + name + "> " + CommonUtils.toLowerFirst(name) + "="
					+ CommonUtils.toLowerFirst(name) + "Repo.findById(id);");
			func.codeLines.add(name + " " + CommonUtils.toLowerFirst(name) + "_tmp=null;");
			func.codeLines
					.add("if(" + CommonUtils.toLowerFirst(name) + ".isPresent()) " + CommonUtils.toLowerFirst(name)
							+ "_tmp=" + CommonUtils.toLowerFirst(name) + ".get();\r\nelse\r\n");
			func.codeLines.add("throw new Exception(\"" + name + " not found\");\r\n");
			String mtmRel = CommonUtils.getPlural((CommonUtils.toLowerFirst(name)));
			if (classes.get(entity).getProperty(mtmRel) != null) {
				func.codeLines.add("List<" + name + "> " + CommonUtils.getPlural(CommonUtils.toLowerFirst(name)) + "="
						+ entityObject + CommonUtils.resolveGetter(null) + CommonUtils.getPlural(name) + "();");
				func.codeLines.add("if(" + CommonUtils.getPlural(CommonUtils.toLowerFirst(name)) + "==null)");
				func.codeLines
						.add(CommonUtils.getPlural(CommonUtils.toLowerFirst(name)) + "=new ArrayList<" + name + ">();");
				func.codeLines.add(CommonUtils.getPlural(CommonUtils.toLowerFirst(name)) + ".add("
						+ CommonUtils.toLowerFirst(name) + "_tmp);");
				func.codeLines.add(entityObject + ".set" + CommonUtils.getPlural(name) + "("
						+ CommonUtils.getPlural(CommonUtils.toLowerFirst(name)) + ");");
			} else
				func.codeLines.add(entityObject + ".set" + name + "(" + CommonUtils.toLowerFirst(name) + "_tmp);");
			if (paramArray != null)
				for (String param : paramArray)
					if (!param.equals(name)) {
						prop = clazz.addProperty("private ", param + "Repository ",
								CommonUtils.toLowerFirst(param) + "Repo", false);
						System.out.println(
								"+++++++++++++++++++++++++" + clazz.getName() + " and " + param + " entity: " + entity);
						// clazz=addPatchMapping(clazz, param);
						prop.annotations.add("@Autowired");
						func.addParam("@RequestParam", "long", CommonUtils.toLowerFirst(param) + "Id");
						func.codeLines.add("Optional<" + param + "> " + CommonUtils.toLowerFirst(param) + "="
								+ CommonUtils.toLowerFirst(param) + "Repo.findById(" + CommonUtils.toLowerFirst(param)
								+ "Id" + ");");
						func.codeLines.add(param + " " + CommonUtils.toLowerFirst(param) + "_tmp=null;");
						func.codeLines.add("if(" + CommonUtils.toLowerFirst(param) + ".isPresent()) "
								+ CommonUtils.toLowerFirst(param) + "_tmp=" + CommonUtils.toLowerFirst(param)
								+ ".get();\r\nelse\r\n");
						func.codeLines.add("throw new Exception(\"" + param + " not found\");\r\n");
						mtmRel = CommonUtils.getPlural((CommonUtils.toLowerFirst(param)));
						if (classes.get(entity).getProperty(mtmRel) != null) {
							func.codeLines.add("List<" + param + "> "
									+ CommonUtils.getPlural(CommonUtils.toLowerFirst(param)) + "=" + entityObject
									+ CommonUtils.resolveGetter(null) + CommonUtils.getPlural(param) + "();");
							func.codeLines
									.add("if(" + CommonUtils.getPlural(CommonUtils.toLowerFirst(param)) + "==null)");
							func.codeLines.add(CommonUtils.getPlural(CommonUtils.toLowerFirst(param))
									+ "=new ArrayList<" + param + ">();");
							func.codeLines.add(CommonUtils.getPlural(CommonUtils.toLowerFirst(param)) + ".add("
									+ CommonUtils.toLowerFirst(param) + "_tmp);");
							func.codeLines.add(entityObject + ".set" + CommonUtils.getPlural(param) + "("
									+ CommonUtils.getPlural(CommonUtils.toLowerFirst(param)) + ");");
						} else {
							func.codeLines.add(
									entityObject + ".set" + param + "(" + CommonUtils.toLowerFirst(param) + "_tmp);");
						}
					}

			func.codeLines.add(entityObject + "=" + entityObject + "Repo.save(" + entityObject + ");");
			func.codeLines.add(entityObject + ".generateLinks();");
			func.codeLines.add("return " + entityObject + ";");
			// System.out.println(func.toString());
		}
		return clazz;
	}

	private void verifyAndResolveRelations(ClassMetaData entity, String rel, String propertyName) {
		ClassMetaData otm = classes.get(rel);
		System.out.println("Adding Relation to: " + rel + "(" + propertyName + ") in " + entity.getName());
		Property otmProp = otm.getProperty(CommonUtils.getPlural(CommonUtils.toLowerFirst(entity.getName())));
		if (otmProp == null)
			return;
		System.out.println("Fixing: " + entity.getName() + " ManyToMany " + rel);
		if (otmProp.removeAnnotation("@OneToMany")) {
			System.out.println("OneToMany removed from: " + rel);
			otmProp.annotations.add("@ManyToMany(mappedBy = \""
					+ CommonUtils.getPlural(CommonUtils.toLowerFirst(otm.getName())) + "\")");
			otmProp.removeAnnotation("@JoinColumn");
			otm.addImport("import javax.persistence.ManyToMany;");
		}

		Property entityMtM = entity.getProperty(propertyName);

		if (entityMtM.removeAnnotation("@OneToMany")) {
			System.out.println("OneToOne Removed from: " + entity.getName());
			entityMtM.annotations.add("@ManyToMany(cascade = CascadeType.ALL)");
			String mtmRel = "@JoinTable(name = \"" + CommonUtils.toLowerFirst(entity.getName()) + "_"
					+ CommonUtils.toLowerFirst(otm.getName()) + "\", \r\n" + "      joinColumns = @JoinColumn(name = \""
					+ CommonUtils.toLowerFirst(entity.getName()) + "_id\", referencedColumnName = \"id\"), \r\n"
					+ "      inverseJoinColumns = @JoinColumn(name = \"" + CommonUtils.toLowerFirst(otm.getName())
					+ "_id\", \r\n" + "      referencedColumnName = \"id\"))";
			entityMtM.annotations.add(mtmRel);
			entity.addImport("import javax.persistence.CascadeType;");
			entity.addImport("import javax.persistence.JoinColumn;");
			entity.addImport("import javax.persistence.JoinTable;");
			entity.addImport("import javax.persistence.ManyToMany;");
			entity.addImport("import javax.persistence.GeneratedValue;");
			entity.addImport("import javax.persistence.GenerationType;");
		}
	}

	private void addHATEAOS(ClassMetaData entity) {
		entity.addImport("import org.springframework.hateoas.Link;");
		entity.addImport("import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;");
		List<Property> properties = entity.getProperties();
		String resource = entity.getName() + "Resource.class";
		Function func = entity.addFunction("public", "void", "generateLinks");
		func.codeLines.add("try{\r\n WebMvcLinkBuilder linkTo = null;");
		for (Property property : properties) {
			if (!property.primitive) {
				String name = property.type.replace("List<", "").replace(">", "");
				if (property.type.contains("List<")) {
					func.codeLines.add("linkTo=WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(" + resource
							+ ").get" + CommonUtils.toUpperFirst(property.name) + "(id));");
					func.codeLines.add("add(Link.of(linkTo.toUriComponentsBuilder().buildAndExpand(id).toString(),\""
							+ CommonUtils.getSingular(property.name) + "\"));");
					// add(Link.of(linkTo.toUriComponentsBuilder().buildAndExpand(id).toString(),"timesheet-details"));
				} else {
					func.codeLines.add("linkTo = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(" + resource
							+ ").get" + CommonUtils.toUpperFirst(property.name) + "(id));");
					func.codeLines.add("add(Link.of(linkTo.toUriComponentsBuilder().buildAndExpand(id).toString(),\""
							+ CommonUtils.toLowerFirst(property.name) + "\"));");
				}
			}
		}
		func.codeLines.add("linkTo = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(" + resource + ").get"
				+ CommonUtils.getPlural(entity.getName()) + "());");
		func.codeLines
				.add("add(Link.of(linkTo.toUriComponentsBuilder().path(\"/{id}\").buildAndExpand(id).toString(),\""
						+ (CommonUtils.toLowerFirst(entity.getName())) + "\"));");
		func.codeLines.add("}catch(Exception e){}");
	}

	public String generateClasses(String packageName, String folderPath, String htmlTemplate, String jsTemplate,
			String cssTemplate, String baseUrl) throws Exception {
		// String packageName=basePackage.getText();
		if (packageName == null || packageName.trim().length() == 0) {
			return ("Please specify base package name like com.example.model");
		}
		String navigationButton = "<a onclick='dynamic_table(\"%%ref%listUrl%\",\"%%ref%editUrl%\")'><button class='button'>%ref%</button></a>";
		String navigation = "";
		Object keys[] = classes.keySet().toArray();
		for (Object key : keys) {
			ClassMetaData clazz = classes.get(key);
			if ("Entity".equals(clazz.getModelType()) && !clazz.getScope().equalsIgnoreCase("local")) {
				navigation += navigationButton.replace("%ref%", clazz.getName());
				// clazz.exportTypeScript(folderPath, packageName);
				addHATEAOS(clazz);
				ClassMetaData resource = classes.get(clazz.getName() + "Resource");
				addGetMapping(resource, clazz.getName(), CommonUtils.toLowerFirst(clazz.getName()));
				addListGetMapping(resource, clazz.getName(),
						CommonUtils.toLowerFirst(CommonUtils.getPlural(clazz.getName())));
				addGetMappingForUniqueProperty(resource);
				resource.addAnnotation("@AllArgsConstructor");
				resource.addImport("import lombok.AllArgsConstructor;");
				resource.addAnnotation("@NoArgsConstructor");
				resource.addImport("import lombok.NoArgsConstructor;");
				addPostMapping(resource, clazz.getName());
				addPutMapping(resource);
				addDeleteMapping(resource);

				List<Property> properties = clazz.getProperties();
				if (properties != null && properties.size() > 0)
					for (Property prop : properties) {
						if (prop.primitive == false && !prop.type.replace("List<", "").replace(">", "")
								.equals(resource.getName().replace("Resource", ""))) {
							if (prop.type.startsWith("List<")) {
								verifyAndResolveRelations(clazz, prop.type.replace("List<", "").replace(">", ""),
										prop.name);
								addPutRelMapping(resource, prop.type.replace("List<", "").replace(">", ""), prop.name);
								addListGetMapping(resource, prop.type.replace("List<", "").replace(">", ""), prop.name);
							} else
								addGetMapping(resource, prop.type, prop.name);
							addPostMapping(resource, prop.type.replace("List<", "").replace(">", ""));
						}
					}
			}
		}

		for (Object key : keys) {
			System.out.println("key:" + key);
			ClassMetaData clazz = classes.get(key);
			if ("api".equalsIgnoreCase(clazz.getModelType())) {
				ClassMetaData resource = classes.get(clazz.getName() + "Resource");
				API api = clazz.getAPI();
				Set<String> imports = api.getImportList();
				if (imports != null)
					for (String imprt : imports) {
						resource.addImport(imprt);
					}

				// resource.addProperty(access, type, name, primitive)
				ClassMetaData payload = classes.get(clazz.getName() + ".Payload");
				String requestBody = "@RequestBody(required = false)";
				if (payload.getProperty("requestpayload").required)
					requestBody = "@RequestBody(required = true)";

				Function apiActionImpl = resource.addFunction("public", "ResponsePayload",
						CommonUtils.toLowerFirst(clazz.getName()));

				apiActionImpl.addParam(requestBody, "RequestPayload", "requestPayload");
				String method = api.method.toLowerCase();
				method = method.substring(0, 1).toUpperCase() + method.substring(1);
				List<Property> queryStringProps = clazz.getProperties();
				ClassMetaData requestheaders = classes.get(clazz.getRoot() + ".Headers" + "." + "RequestHeaders");

				List<Property> properties = requestheaders.getProperties();
				if (properties != null && properties.size() > 0) {
					resource.addImport("import org.springframework.web.bind.annotation.RequestHeader;");
					requestheaders.addAnnotation("@AllArgsConstructor");
					requestheaders.addImport("import lombok.AllArgsConstructor;");
					requestheaders.addAnnotation("@NoArgsConstructor");
					requestheaders.addImport("import lombok.NoArgsConstructor;");
					for (Property property : properties) {
						apiActionImpl.addParam("@RequestHeader(\"" + property.name + "\")", property.type,
								property.name);
					}
				}
				// @PostMapping(path = "/entrys",params= {"projectId","timesheetId"})
				// @RequestParam long timesheetId
				// @PathVariable long id
				String params = "";

				api.path = clazz.getProperty("path").value;
				for (Property property : queryStringProps) {
					if (property.primitive && !property.name.equalsIgnoreCase("path")) {
						if (!api.path.contains("{" + property.name + "}")) {
							apiActionImpl.addParam("@RequestParam", property.type, property.name);
							params += "\"" + property.name + "\",";
						} else {
							apiActionImpl.addParam("@PathVariable", property.type, property.name);
						}
					}
				}
				if (params.trim().length() > 0)
					params = (params + ",").replace(",,", "");
				// @RequestHeader("category") String category
				// api.addQueryParam(name, type);
				apiActionImpl.annotations
						.add("@" + method + "Mapping(path=\"" + api.path + "\", params={" + params + "})");
				apiActionImpl.codeLines.add(new String(Base64.getDecoder().decode(api.code)));
				resource.setCustomCode(new String(Base64.getDecoder().decode(api.custom)));
				clazz.exportAPI(folderPath, packageName, classes);
			}
		}
		System.out.println("navigation: " + navigation);
		for (Object key : keys) {
			ClassMetaData clazz = classes.get(key);
			clazz.exportJava(folderPath, packageName);
			navigation = clazz.exportGUI(folderPath, navigation, baseUrl);
		}
		System.out.println("navigation-updated: " + navigation+"\n"+packageName);
		String tempPackage=packageName.replace(".", "_");
		//String modelName=new PromptDialogBox(, style)
		
		String modelName=(tempPackage.split("_")[tempPackage.split("_").length-2]);
		//System.out.println("Model name:"+modelName+" base url:"+baseUrl);
		String dirPath = folderPath.split("java")[0] + "resources/static/"+modelName+"-admin-ui/";
		String data = htmlTemplate.replace("%navigation%", navigation).replace("%baseUrl%", baseUrl);
		File file = new File(dirPath);
		if (!file.exists() || !file.isDirectory()) {
			file.mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(new File(dirPath + "/index.html"));
		fos.write(data.getBytes());
		fos.flush();
		fos.close();

		fos = new FileOutputStream(new File(dirPath + "/DynamicTableJs.js"));
		fos.write(jsTemplate.getBytes());
		fos.flush();
		fos.close();

		fos = new FileOutputStream(new File(dirPath + "/DynamicTableCss.css"));
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
