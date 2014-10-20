import java.io.*; 
import java.util.HashMap;


public class FileIndex { 
  
	private int ID;
	private static String indexPath = new String("/home/neal/workspace/SearchEngine/index/");
	private static String dataPath = new String("/home/neal/workspace/SearchEngine/data/Data/");
	private HashMap<String, Integer> TokenHash;
	
	
	public FileIndex() { 
	} 
	
	/* Create the Index from the source file , the file will be cut to English token , 
	 * Chinese token , and Number token.*/
	public void CreateIndex( ){
		
		File source = new File(dataPath);
		String token = "", chinese = "";
		
		try{
			// search data 資料夾內的檔案 , 是txt檔的就讀
			File[ ] file = source.listFiles();
			
			for(int i=0 ; i<file.length ; i++){
				if(file[i].getName().endsWith(".txt")){
					ID = i;
					FileReader fin = new FileReader (file[i]);
					TokenHash = new HashMap<String, Integer>( );
					
					int word, preword = 0;		
					while(fin.ready( ) ) {
						word = fin.read( );
						
						if( word >= 65 && word <=90 || word>=97 && word<=122 ){			// English term
							if(preword != 0){
								System.out.println();
								token = token.toLowerCase();
								token = ClassifyString(token);
								if(chinese != ""){
									chinese = ClassifyString(chinese);
								}
							}
							token += (char)word;
							System.out.print( (char)word );
							preword = 0;
						}
						else if( word >=  '\u4e00' && word <= '\u9fa5' ){				// Chinese term
							if(preword != 1 && token != ""){
								System.out.print( "," + ID );
								token = token.toLowerCase();
								token = ClassifyString(token); 
							}
							preword = 1;
							System.out.println();
							System.out.print( (char)word + "," + ID );
							chinese += (char)word;
						}
						else if( word >= 48 && word <= 57 ){								// number term
							if(preword == 0){
								System.out.print( "," + ID );
								token = token.toLowerCase();
								token = ClassifyString(token);
							}
							else if (preword == 1){
								System.out.println();
								token = token.toLowerCase();
								token = ClassifyString(token);
								if(chinese != ""){
									chinese = ClassifyString(chinese);
								}
							}
							token += (char)word;
							System.out.print( (char)word );
							preword = 2;
							
						}
						else{																				// 其他類
							if(token != ""){
								token = token.toLowerCase();	
								token = ClassifyString(token);
							}
							else if(chinese != "")
								chinese = ClassifyString(chinese);
							
							if(preword != 2 && preword != 1 && preword != 3 ){
								System.out.print( "," + ID );
							}
							preword = 3;
						}
					}
					fin.close();
					PrintHash();					// 每次讀完file, 都把 hash內的值 存到file內
					ID++;
				}
				
			}
		
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	// the cut token should be stored in the TokenHash
	public String ClassifyString(String s){
		
		int value = 1;
		String token = s;
		try{
			// store in HashMap , 2-gram
			if ( token != "" && ( token.charAt(0) >=  '\u4e00' && token.charAt(0) <= '\u9fa5' ) ){
				
				// token 大於兩個位元的情況下 , 去切
				if(token.length() > 1){
					for(int i = 0 ; i < token.length() ; i++){
						if( i != token.length()-1 ){
							if( TokenHash.get(token.substring(i, i+2)) != null ){
								value = TokenHash.get(token.substring(i, i+2)) + 2;
								TokenHash.put(token.substring(i, i+2), value);
							}
							else
								TokenHash.put(token.substring(i, i+2), 2);
						}
						// 2-gram 表示切成 :  qaz --> qa, az, q, a, z 
						if( TokenHash.get(token.charAt(i)+"") != null ){
							value = TokenHash.get(token.charAt(i)+"") + 1;
							TokenHash.put(token.charAt(i)+"", value);
						}
						else
							TokenHash.put(token.charAt(i)+"", 1);
					}
				}
				//token 只有一個中文字
				else{
					if( TokenHash.get(token+"") != null ){
						value = TokenHash.get(token+"") + 1;
						TokenHash.put(token+"", value);
					}
					else
						TokenHash.put(token+"", 1);
				}
			}
			// 除了國字之外 , 其他的直接存在 hash 內
			else if( TokenHash.get(token) != null ){
				value = TokenHash.get(token) + 1;
				TokenHash.put(token, value);
			}
			else
				TokenHash.put(token, 1);
			
			token="";
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return token;	
	}
	
	/* write the cut token into the index file , use HashCode to separate the index file
	     100*26 files for English token , 500 files for Chinese token , 500 files for Number token */
	public void PrintHash(){
		
		try{
			for( String name:TokenHash.keySet( ) ){
				// 存入到index內, 用自首a~z來區分
				if( name!="" && (name.charAt(0) >= 65 && name.charAt(0) <= 90 || name.charAt(0) >= 97 && name.charAt(0) <= 122) ){
					FileWriter finx = new FileWriter(indexPath + name.charAt(0)  + "_" + name.hashCode()%50 + ".ind", true);
					finx.write(name + "," + TokenHash.get(name) + "," + ID + "\n");
					finx.close();
				}
				 // 存在num.ind 的 index 內
				else if ( name!=""&& (name.charAt(0) >= 48 && name.charAt(0) <= 57) ){
					FileWriter fnum = new FileWriter(indexPath + "num" + "_" + name.hashCode()%500 + ".ind", true);
					fnum.write(name +  "," + TokenHash.get(name) + "," + ID + "\n");
					fnum.close();
				}
				// store in chinese.ind's index , 2-gram
				else if ( name != "" && ( name.charAt(0) >=  '\u4e00' && name.charAt(0) <= '\u9fa5' ) ){
					FileWriter fwCh = new FileWriter (indexPath + "chinese " + "_" + name.hashCode()%500 + ".ind", true);		
					fwCh.write(name + "," + TokenHash.get(name) + "," + ID + "\n");
					fwCh.close();
					}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String args[ ] ) throws IOException { 
		
		FileIndex f = new FileIndex();
		File testF = new File(indexPath);
		File[ ] file = testF.listFiles();
		SearchFrame SF = new SearchFrame();
		try{
			if(file.length == 0)					// if index is not created, then create.
				f.CreateIndex();
			System.out.println("Index is already created!!");
		}catch(Exception e){
			e.printStackTrace();
		}
	}	 

	
	
}