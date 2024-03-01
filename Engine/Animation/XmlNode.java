package Engine.Animation;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;
public class XmlNode
{
	public XmlNode(){}
	
	public XmlNode load(InputStream ins){
		try{
		DocumentBuilderFactory doc = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = doc.newDocumentBuilder();
		root =  builder.parse(ins);
		}catch(Exception e){
			System.out.println("error :"+e.toString());
		}
		return this;
	}
	
public XmlNode load(String s){
	try{
	DocumentBuilderFactory doc = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = doc.newDocumentBuilder();
	root =  builder.parse(new FileInputStream(s));
	}catch(Exception e){
		System.out.println("error :"+e.toString());
	}
	return this;
	}
	private Node root;
	public String getData(){
	return root.getTextContent();
	}
	public XmlNode getChild(String str){
	NodeList list = root.getChildNodes();
	for(int i=0;i < list.getLength();i++){
	Node nd = list.item(i);
		if(nd.getNodeName().equals(str)){
		XmlNode xm = new XmlNode();
		xm.root = nd;
		return xm;
	}
	}
	return null;
	}
	
	public String getName(){
	return root.getNodeName();
	}
	
	public List<XmlNode> getChilds(){
	ArrayList<XmlNode> tmp = new ArrayList<>();
	NodeList list = root.getChildNodes();
	for(int i=0;i < list.getLength();i++){
	Node nd = list.item(i);
	XmlNode xm = new XmlNode();
	xm.root = nd;
	tmp.add(xm);
	}
	return tmp;
	}
	
	public String getAttribute(String sr){
	Node ds= root.getAttributes().getNamedItem(sr);
	return ds.getNodeValue();
	}
	
	public List<XmlNode> getChilds(String str){
		ArrayList<XmlNode> tmp = new ArrayList<>();
		NodeList list = root.getChildNodes();
		for(int i=0;i < list.getLength();i++){
			Node nd = list.item(i);
			if(nd.getNodeName().equals(str)){
			XmlNode xm = new XmlNode();
			xm.root = nd;
			tmp.add(xm);
			}
		}
		return tmp;
	}
	
	public boolean hasAttribute(String til){
		if(root.getAttributes() == null){
		return false;
		}
	return root.getAttributes().getNamedItem(til)!=null;
	}
	
	
	public boolean hasAttributeWithValue(String str,String val){
		try{
		if(!hasAttribute(str)){
			return false;
			}
	return root.getAttributes().getNamedItem(str).getNodeValue().equals(val);
	}catch(Exception e){
	return false;
	}
	}
	
	public XmlNode getChildWithAttributeValue(String child,String attrib,String val){
	try{
		for(XmlNode b : getChilds(child)){
		if(b.hasAttribute(attrib)){
			if(b.getAttribute(attrib).equals(val)){
		return b; }
		}
		}
	}catch(Exception e){
	return null;
	}
	return null;
	}
}
