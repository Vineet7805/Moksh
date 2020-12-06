package com.github.moksh.generator.core;

import java.io.FileReader;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ImportJSON {
	public String jsonWorkspace="";
	public static final String identifier="-~-~-~-010-~-~-~";
	public Map<String, Object> schemaMap;
	public static String POM=null;
	public static String APP=null;
	
	public ImportJSON() {
		// TODO Auto-generated constructor stub
		schemaMap=new HashMap<String, Object>();
	}

 	public static void main(String[] args) throws Exception {
		//load("D:\\nvme\\data\\Sample.JSON");
 		
		String[] jsonWorkspace= new ImportJSON().loadSchema("D:\\nvme\\data\\SampleSchema.JSON");
		//System.out.println(jsonWorkspace[0]);
		//System.out.println(jsonWorkspace[1]);
	}
 	private static ImportJSON ij=null;
 	public static ImportJSON getInstance() {
 		if(ij==null)
 			 ij=new ImportJSON();
 		return ij;
 	}
 	
	
	public static String beautify(String jsonStr) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		//System.out.println(jsonStr);
		JsonElement je = jp.parse(jsonStr);
		String prettyJsonString = gson.toJson(je);
		return prettyJsonString;
	}
	
	public String[] loadPayload(String fileName) throws Exception {
		Object obj = new JSONParser().parse(new FileReader(fileName)); 
        JSONObject jo = (JSONObject) obj;
        String org=jo.toJSONString();
        jsonWorkspace="";
        generateJSONSchema(jo,1);
        jsonWorkspace="{\n"+jsonWorkspace.replaceAll(","+identifier, "")+"\n}";
        jsonWorkspace=jsonWorkspace.replaceAll(identifier, "");
        //Object schemaObj = new JSONParser().parse(schema);
        //jo = (JSONObject) schemaObj;
        //System.out.println(schema);
        return new String[] {beautify(jsonWorkspace),beautify(org)};
	}
	
	public String[] loadSchema(String fileName) throws Exception {
		Object obj = new JSONParser().parse(new FileReader(fileName)); 
        JSONObject jo = (JSONObject) obj;
        String org=jo.toJSONString();
        if(jo.get("pom")!=null)
        	POM=decodeBase64(jo.get("pom").toString());
        if(jo.get("application")!=null)
        	APP=decodeBase64(jo.get("application").toString());
        jo=(JSONObject)jo.get("properties");
//        if(jo.get("type")!=null)
//        	jo=(JSONObject)(new JSONParser().parse("{\"_root\":"+org+"}"));
        jsonWorkspace="";
        schemaMap=generateJsonPayload(jo,1,"object");
        jsonWorkspace="{\n"+jsonWorkspace.replaceAll(","+identifier, "")+"\n}";
        jsonWorkspace=jsonWorkspace.replaceAll(identifier, "");
        //Object schemaObj = new JSONParser().parse(schema);
        //jo = (JSONObject) schemaObj;
        //System.out.println(schema);
        //return null;

        return new String[] {beautify(org),beautify(jsonWorkspace)};
	}
	
	public static String decodeBase64(String string) {
		   try {
			   return new String(Base64.getDecoder().decode(string));
		    } catch(Exception e) {   
		    	return string;
		    }
		}
	public static String encodeBase64(String data) {
		return Base64.getEncoder().encodeToString(data.getBytes());
	}
	
	public String[] generatePayload(String schemaStr) throws Exception {
		Object obj = new JSONParser().parse(schemaStr); 
        JSONObject jo = (JSONObject) obj;
        String orgSchema=jo.toJSONString();
        //if(jo.get("type")!=null)
        jo=(JSONObject)jo.get("properties");
        //jo=(JSONObject)(new JSONParser().parse("{\"_root\":"+org+"}"));
        jsonWorkspace="";
        //schemaMap.clear();
        schemaMap=generateJsonPayload(jo,1,"object");
        jsonWorkspace="{\n"+jsonWorkspace.replaceAll(","+identifier, "")+"\n}";
        jsonWorkspace=jsonWorkspace.replaceAll(identifier, "");
        //Object schemaObj = new JSONParser().parse(schema);
        //jo = (JSONObject) schemaObj;
        //System.out.println(schema);
        //return null;
        //System.out.println(org);
        //System.out.println(jsonWorkspace);
        return new String[] {beautify(orgSchema),beautify(jsonWorkspace)};
	}
	
	public String[] generateSchema(String jStr) throws Exception {
		Object obj = new JSONParser().parse(jStr); 
		String org="";
		JSONObject jo=null;
		if(obj instanceof JSONArray) {
        	org=((JSONArray) obj).toJSONString();
        	String json="{\"_root\":"+org+"}";
        	obj = new JSONParser().parse(json);
        	jo= (JSONObject) obj;
        }else {
        	jo= (JSONObject) obj;
        	org=jo.toJSONString();
        }
        jsonWorkspace="";
        generateJSONSchema(jo,1);
        jsonWorkspace="{\n "
        		+ "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\r\n" + 
        		"  \"type\": \"object\",\r\n" + 
        		"  \"properties\": {"+jsonWorkspace.replaceAll(","+identifier, "")+"\n}\n}";
        jsonWorkspace=jsonWorkspace.replaceAll(identifier, "");
        //Object schemaObj = new JSONParser().parse(schema);
        //jo = (JSONObject) schemaObj;
        //System.out.println(jsonWorkspace);
        //System.out.println(org);
        //System.out.println(jsonWorkspace);
        return new String[] {beautify(jsonWorkspace),beautify(org)};
	}
	
	private void appendln(String data) {
		jsonWorkspace+=data+"\n";
		//System.out.println(data.replaceAll(","+identifier, "").replaceAll(identifier, ""));
	}
	
	private void append(String data) {
		jsonWorkspace+=data;
		//System.out.print(data.replaceAll(","+identifier, "").replaceAll(identifier, ""));
	}
	
	public static String padding(int num) {
		String pad="";
		for(int pa=0;pa<=num;pa++)
    		pad+="  ";
		return pad;
	}
	
	private Map<String, Object> generateJsonPayload(JSONObject jo, int root,String parentType) throws Exception {
		if(jo==null)
			return new HashMap<String, Object>();
		Object[] keys=jo.keySet().toArray();
		Map<String, Object> localMap=new HashMap<String, Object>();
        for (Object object : keys) {
        	Map<String, Object> dataMap=null;
        	String key=object.toString();
        	//System.out.println(key);
        	JSONObject keyObj=(JSONObject)jo.get(key);
        	//System.out.println(keyObj.get("type"));
        	String type=(String) keyObj.get("type");
        	String required=(String) keyObj.get("required");
        	String desc=(String) keyObj.get("description");
        	String title=(String) keyObj.get("title");
        	String format=(String) keyObj.get("format");
        	String example=(String) keyObj.get("example");
        	String enm=(keyObj.get("enum")+"").replace("null", "").trim();
        	String scope=(keyObj.get("scope")+"").replace("null", "").trim();
        	String code=(keyObj.get("code")+"").replace("null", "").trim();
        	String imports=(keyObj.get("import")+"").replace("null", "").trim();
        	String custom=(keyObj.get("custom")+"").replace("null", "").trim();
        	if(enm.length()>=2) {
        		if(enm.endsWith("]"))
            		enm=enm.substring(0, enm.length()-1);
	        	if(enm.startsWith("["))
	        		enm=enm.replaceFirst("\\[", "");
        	}
        	
        	if(type==null)
        		throw new Exception("Schema type missing in one of the spec.");

        	if(type.toLowerCase().startsWith("api<")) {
        		appendln("\n"+padding(root)+"\""+key+"\" : {");
        		//schemaMap.put(key, value);
        		dataMap=generateJsonPayload((JSONObject)keyObj.get("properties"), root+1,"object");
        		dataMap.put("type",type);
        		dataMap.put("required",required);
        		dataMap.put("title",title);
        		dataMap.put("description",desc);
        		dataMap.put("scope",scope);
        		if(code!=null && code.trim().length()>0)
        			dataMap.put("code",code);
        		if(custom!=null && custom.trim().length()>0)
        			dataMap.put("custom",custom);
        		if(imports!=null && imports.trim().length()>0)
        			dataMap.put("import",imports);
        		localMap.put(key, dataMap);
        		append(padding(root)+"},");
        	}else
        	if(type.equalsIgnoreCase("object")) {
        		appendln("\n"+padding(root)+"\""+key+"\" : {");
        		//schemaMap.put(key, value);
        		dataMap=generateJsonPayload((JSONObject)keyObj.get("properties"), root+1,"object");
        		dataMap.put("type","object");
        		dataMap.put("required",required);
        		dataMap.put("title",title);
        		dataMap.put("description",desc);
        		dataMap.put("scope",scope);
        		localMap.put(key, dataMap);
        		append(padding(root)+"},");
        	}
        	else if(type.equalsIgnoreCase("array")) {
        		
        		String subType="";
        		if(keyObj.get("items") instanceof JSONArray) {
        			appendln("\n"+padding(root)+"\""+key+"\" : [");
        			subType=((JSONObject)(((JSONArray)keyObj.get("items")).get(0))).get("type").toString();
        			//System.out.println("subtype: "+subType);
        			dataMap=generateJsonPayload(null, 0, null);
        			if(example==null || example.trim().length()<1)
        				example=getSampleValue(subType);
        			//System.out.println("Example: "+example);
        			appendln("\n"+padding(root+1)+"\""+example+"\"");
        			dataMap.put("type","array<"+subType+">");
        			dataMap.put("required",required);
        			dataMap.put("title",title);
            		dataMap.put("description",desc);
            		dataMap.put("example",example);
            		dataMap.put("scope",scope);
            		localMap.put(key, dataMap);
            		append(padding(root)+"],");
        		}
        		else {
        			appendln("\n"+padding(root)+"\""+key+"\" : [{");
        			dataMap=generateJsonPayload((JSONObject)((JSONObject)keyObj.get("items")).get("properties"), root+1,"array");
	        		dataMap.put("type","array<object>");
	        		dataMap.put("required",required);
	        		dataMap.put("title",title);
	        		dataMap.put("description",desc);
	        		dataMap.put("scope",scope);
	        		localMap.put(key, dataMap);
	        		append(padding(root)+"}],");
        		}
        		
        	}else {
        		//if(parentType.equals("array"))
        		//	append(padding(root)+"\""+key+"\":");
        		//else
        		append(padding(root)+"\""+key+"\":");
        		dataMap=new HashMap<String, Object>();
        		dataMap.put("type",type);
        		dataMap.put("required",required);
        		dataMap.put("title",title);
        		dataMap.put("description",desc);
        		dataMap.put("enum",enm);
        		dataMap.put("format",format);
        		dataMap.put("example",example);
        		dataMap.put("scope",scope);
        		localMap.put(key, dataMap);
        		String val="\"TEXT\"";
        		if(example!=null && example.trim().length()>0){
        			val="\""+example+"\"";
        		}else
        			val="\""+getSampleValue(type)+"\"";
        		if(parentType.equals("array"))
        			append(padding(root)+val+",");
        		else
        			append(padding(root)+val+",");
        	}
        }
        appendln(identifier);
        return localMap;
	}
	
	private String getSampleValue(String type) {
		String val="TEXT";
		switch (type) {
		case "integer":
			val="1";
			break;
		case "number":
			val="12.1234";
			break;
		case "float":
			val="1.5";
			break;
		case "double":
			val="1.02";
			break;
		case "string":
			val="TEXT";
			break;
		default:
			break;
		}
		return val;
	}
	
	private void generateJSONSchema(JSONObject jo, int root) {
		Object[] keys=jo.keySet().toArray();
        for (Object object : keys) {
        	
        	String key=object.toString();
        	if(jo.get(key) instanceof JSONArray) {
        		appendln("\n"+padding(root)+"\""+key+"\" : {");//Class
        		appendln(padding(root+2)+"\"type\":\"array\",");
        		appendln(padding(root+2)+"\"title\":\"Array of "+key+"\",");
        		appendln(padding(root+2)+"\"description\":\"Description of array of "+key+"\",");
        		if(((JSONArray)jo.get(key)).size()==0){
        			appendln(padding(root+2)+"\"items\":[");
        			appendln(padding(root+3)+"{\"type\":\"string\"}");
        			appendln(padding(root+2)+"]");
        		}else
        		if(!(((JSONArray)jo.get(key)).get(0) instanceof JSONObject) ) {
        			String val=""+((JSONArray)jo.get(key)).get(0);
        			//System.out.println("value: "+val);
        			appendln(padding(root+2)+"\"example\":\""+val+"\",");
        			appendln(padding(root+2)+"\"items\":[");
        			appendln(padding(root+3)+"{\"type\":\""+getType(val)+"\"}");
        			appendln(padding(root+2)+"]");
        		}
        		else {
	        		appendln(padding(root+2)+"\"items\":{");
	        		appendln(padding(root+3)+"\"type\":\"object\",");
	        		appendln(padding(root+3)+"\"title\":\"Title for "+key+"\",");
	        		appendln(padding(root+3)+"\"description\":\"Description for "+key+"\",");
	        		appendln(padding(root+3)+"\"properties\":{");
	        		generateJSONSchema((JSONObject) ((JSONArray)jo.get(key)).get(0),root+4);
	        		appendln(padding(root+3)+"}");
	        		appendln(padding(root+2)+"}");
        		}
        		append(padding(root)+"},");
        		//}
			}
        	else if(jo.get(key) instanceof JSONObject) {
        		appendln("\n"+padding(root)+"\""+key+"\" : {");//Class
        		appendln(padding(root+2)+"\"type\":\"object\",");
        		appendln(padding(root+2)+"\"title\":\"Title for "+key+"\",");
        		appendln(padding(root+2)+"\"description\":\"Description for "+key+"\",");
        		appendln(padding(root+2)+"\"properties\":{");
        		generateJSONSchema((JSONObject) jo.get(key),root+3);
        		appendln(padding(root+2)+"}");
        		append(padding(root)+"},");
			}else {
				//property name
				appendln("\n"+padding(root)+"\""+key+"\" : {");//Class
        		appendln(padding(root+2)+"\"type\":\""+getType((jo.get(key)+"").replace("null", ""))+"\",");
        		appendln(padding(root+2)+"\"title\":\"Title for "+key+"\",");
        		appendln(padding(root+2)+"\"description\":\"Description for "+key+"\",");
        		appendln(padding(root+2)+"\"example\":\""+((jo.get(key)+"").replace("null", ""))+"\"");
				append(padding(root)+"},");
				//property type
				//appendln(padding(root)+jo.get(key).toString());
			}
		}
        appendln(identifier);
	}
	
	public static String getType(String text) {
		try {
			Integer.parseInt(text);
			return "integer"; 
		} catch (Exception e) {
			try {
				Double.parseDouble(text);
				return "number";
			} catch (Exception e2) {
				return "string";
			}
		}
	}
}
