import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SearchFrame implements ActionListener {
	
	private static String indexPath = new String("/home/neal/workspace/SearchEngine/index/");
	private static String dataPath = new String("/home/neal/workspace/SearchEngine/data/Data/");
	private JTextArea QueryArea;
	// query match, frequency and file's number
	private IdentityHashMap<Integer, Integer> FreqFHash = new IdentityHashMap<Integer, Integer> ();					
	// how many times query hits file, file's number and hit numbers
	private HashMap<Integer, Integer> HitHash = new HashMap<Integer, Integer>( );													
	private String listFile;
	private int queryNum;			// query's token number
			
	JFrame SE = new JFrame("Neal's Search Engine");
	JPanel Pwhole = new JPanel();
	JPanel SearchBar = new JPanel();
	JLabel LabelName = new JLabel("Search Engine ");
	JButton SearchB = new JButton("Search");
	JPanel listPanel = new JPanel();
	JLabel NotF = new JLabel("File not found !");
	DefaultListModel<String> DLM = new DefaultListModel<String>();
	JList<String> listbox = new JList<String>(DLM);
	
	/* Initialize Frame and components inside the Frame */
	public SearchFrame( ) {

			Pwhole.setBackground(Color.LIGHT_GRAY);
			Pwhole.setPreferredSize(new Dimension(800,50) );
			SearchBar.setBackground(Color.LIGHT_GRAY);
			
			LabelName.setFont(new Font("Monospace", Font.BOLD, 28) );
			
			SE.setSize(800,600);
			SE.setLocation(500,100);
			SE.setResizable(false);	
			SE.setLocationRelativeTo(null);
			SE.getContentPane();
			
			QueryArea = new JTextArea(1,20);
			QueryArea.setFont(new Font("Monospace", Font.PLAIN, 20) );
			
			SearchB.setBackground(Color.cyan);
			SearchB.setFont(new Font("Monospace", Font.PLAIN, 16));

			SE.setVisible(true);
			SE.getContentPane().setLayout(new FlowLayout());
			
			SearchBar.add(LabelName);
			Pwhole.add(SearchBar);
			Pwhole.add(QueryArea);
			Pwhole.add(SearchB);
			SE.getContentPane().add(Pwhole);
			
			SearchB.addActionListener(this);
			QueryArea.addKeyListener(new KeyListener(){
				
				@Override
				public void keyPressed(KeyEvent e){
					if(e.getKeyCode() == KeyEvent.VK_ENTER)
						SearchB.doClick();
				}
				@Override
				public void keyTyped(KeyEvent e) {
				}
				@Override
				public void keyReleased(KeyEvent e) {
					String s = QueryArea.getText();
					if(e.getKeyCode() == KeyEvent.VK_ENTER && s.length() != 0){
						if(s.charAt(s.length()-1) == '\n')
							QueryArea.setText(s.substring(0, s.length()-1));
				    	}
				    }
			});
			
			SE.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
			listbox.setFont(new Font("Monospace", Font.BOLD, 24) );				
			listPanel.setLayout(new FlowLayout() );
			listPanel.add(listbox);
			JScrollPane scrollPane = new JScrollPane(listPanel);
			listPanel.setAutoscrolls(true);
			scrollPane.setPreferredSize(new Dimension(600,500));
			SE.getContentPane().add(scrollPane);
			NotF.setFont(new Font("Monospace", Font.BOLD, 24) );	
			listPanel.add(NotF);
			NotF.setVisible(false);
	}
	
	/* 事件觸發 , 按button or enter 鍵時會發生 */
	public void actionPerformed(ActionEvent e){
		
		try{
            int start = QueryArea.getLineStartOffset(0);  
            int end =  QueryArea.getLineEndOffset(0);  
            String getContent = QueryArea.getText(start, end-start);
            getContent += "\n";
 //           System.out.println(getContent.hashCode());
            listFile="";
            queryNum = 0;
            FreqFHash.clear();
            HitHash.clear();
            QueryAnalysis(getContent);
            SortQuery();
            ShowHitFile();
                     
		}catch(Exception ee){
			ee.printStackTrace();
		}
		return;
	}
	
	/* 整串 query 丟進來做分析 , 一樣先切 中 英 數字 等 */ 
	public void QueryAnalysis(String getContent){
		char[ ] word = getContent.toCharArray();
		String SW = "", chinese = "";
		int prechar = 0;
		
		try{
			for(int i = 0 ; i<getContent.length() ; i++ ){			
				if( word[i] >= 65 && word[i] <= 90 || word[i] >= 97 && word[i] <= 122 ){			// English term
					if(prechar != 0){
						if(SW != ""){
							SW = SW.toLowerCase();
							SW = SearchIndex(SW);
						}
						if(chinese != ""){
							chinese = SearchIndex(chinese);
						}
					}
					SW += word[i];
					prechar = 0;
				}
				else if(word[i] >=  '\u4e00' && word[i] <= '\u9fa5'  ){								// Chinese term
					if(prechar != 1 && SW != ""){
						SW = SW.toLowerCase();
						SW = SearchIndex(SW);
					}
					chinese += word[i];
					prechar = 1;
				}
				else if( word[i] >= 48 && word[i] <= 57 ){													// number term
					if(prechar == 0 && SW!=""){
						SW = SW.toLowerCase();
						SW = SearchIndex(SW);
					}
					else if (prechar == 1){
						SW = SW.toLowerCase();
						SW = SearchIndex(SW);
						if(chinese != ""){
							chinese = SearchIndex(chinese);
						}
					}
					SW += (char)word[i];
					prechar = 2;
				}
				else{																						// others
					if(SW != ""){
						SW = SW.toLowerCase();	
						SW = SearchIndex(SW);
					}
					else if(chinese != "")
						chinese = SearchIndex(chinese);
					
					prechar = 3;
				}
			}			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/* cutting off the query and Search it from Index , build the HashMap which query matches */ 
	public String SearchIndex(String q){
		int value = 1;
		File source = new File(dataPath);
		File[ ] file = source.listFiles();
		
		try{

			// For English Query
			if( (q.charAt(0) >= 65 && q.charAt(0) <= 90) || (q.charAt(0) >= 97 && q.charAt(0) <= 122) ){
				FileReader fr = new FileReader(indexPath + q.charAt(0) + "_" + q.hashCode()%50 + ".ind" );
				BufferedReader br = new BufferedReader(fr);
				queryNum ++;
				
				while( br.ready() ){
					String[ ] str = br.readLine().split(",");
					if(str[0].equals(q)){
						FreqFHash.put( Integer.valueOf(str[1]),  Integer.valueOf(str[2]) );
						System.out.println(str[0]+ "," +Integer.valueOf(str[1]) + "," + file[Integer.valueOf(str[2])].getName() );
						
						if(HitHash.get( Integer.valueOf(str[2]) ) != null){
							value = HitHash.get( Integer.valueOf(str[2]) ) + 1;
							HitHash.put(Integer.valueOf(str[2]), value);
							System.out.println( Integer.valueOf(str[2])+ "  "+value);
						}
						else
							HitHash.put(Integer.valueOf(str[2]), value);
					}
				}
				fr.close();
			}
			//	For Chinese Query
			else if( q != "" && ( q.charAt(0) >=  '\u4e00' && q.charAt(0) <= '\u9fa5' ) ){
				HashMap<String, Integer> KeywordHash = new HashMap<String, Integer> ( );			// Search keyword 分析 for Chinese
				
				// q 大於兩個位元的情況下 , 去切
				if(q.length() > 1){
					for(int i = 0 ; i < q.length() ; i++){
						// 2-gram 表示切成 :  qaz --> qa, az, q, a, z 
						if( i != q.length()-1 ){
							queryNum ++;
							if( KeywordHash.get(q.substring(i, i+2)) != null ){
								value = KeywordHash.get(q.substring(i, i+2)) + 1;
								KeywordHash.put(q.substring(i, i+2), value);
							}
							else
								KeywordHash.put(q.substring(i, i+2), 1);
						}
						if( KeywordHash.get(q.charAt(i)+"") != null ){
							value = KeywordHash.get(q.charAt(i)+"") + 1;
							KeywordHash.put(q.charAt(i)+"", value);
						}
						else
							KeywordHash.put(q.charAt(i)+"", 1);
						
					}
				}
				//q 只有一個中文字
				else{
					queryNum ++;
					if( KeywordHash.get(q+"") != null ){
						value = KeywordHash.get(q+"") + 1;
						KeywordHash.put(q+"", value);
					}
					else
						KeywordHash.put(q+"", 1);
				}
				
				for(String name:KeywordHash.keySet()){
					System.out.println(name + " " + name.hashCode()%500);
					FileReader fr = new FileReader(indexPath + "chinese " + "_" + name.hashCode()%500 + ".ind" );
					BufferedReader br = new BufferedReader(fr);
					
					while(br.ready() ){
						String[ ] str = br.readLine().split(",");
						if( name.length() > 1 ){											// query的中文字大於兩個
							for(int i = 0 ; i < name.length() ; i++){
								if( i != name.length()-1 && str[0].equals( name.substring(i, i+2) ) ){		// 比較兩個字的
									FreqFHash.put( new Integer (Integer.valueOf(str[1]) ) ,  Integer.valueOf(str[2]) );
									System.out.println(str[0]+ "," +Integer.valueOf(str[1]) + "," + file[Integer.valueOf(str[2])].getName() + ">=2");
										
									if(HitHash.get( Integer.valueOf(str[2]) ) != null){
										value = HitHash.get( Integer.valueOf(str[2]) ) + 1;
										HitHash.put(Integer.valueOf(str[2]), value);
									}
									else
										HitHash.put(Integer.valueOf(str[2]), value);
								}
							}
						}
						else if( str[0].matches(name + ".*") && q.length()<2 ){
							FreqFHash.put( new Integer(Integer.valueOf(str[1])),  Integer.valueOf(str[2]) );
							System.out.println(str[0]+ "," +Integer.valueOf(str[1]) + "," + file[Integer.valueOf(str[2])].getName() + " == 1");
						}
					}
					br.close();
				}
			}
			// For Number Query 
			else if( q.charAt(0) >= 48 && q.charAt(0) <= 57){
				FileReader fr = new FileReader(indexPath + "num" + "_" + q.hashCode()%500 + ".ind" );
				BufferedReader br = new BufferedReader(fr);
				queryNum ++;
				
				while( br.ready() ){
					String[ ] str = br.readLine().split(",");
					if(str[0].matches(q)){
						FreqFHash.put( Integer.valueOf(str[1]),  Integer.valueOf(str[2]) );
						System.out.println(str[0]+ "," +Integer.valueOf(str[1]) + "," + file[Integer.valueOf(str[2])].getName() );
						if(HitHash.get( Integer.valueOf(str[2]) ) != null){
							value = HitHash.get( Integer.valueOf(str[2]) ) + 1;
							HitHash.put(Integer.valueOf(str[2]), value);
						}
						else
							HitHash.put(Integer.valueOf(str[2]), value);
					}
				}
				fr.close();
			}
			else
				System.out.println("command not found. ");
			
			q = "";
		}catch(Exception e){
			e.printStackTrace();
		}
		return q;
	}
	
	/* Sort the file which hits the query more */
	public void SortQuery( ){
		int Hittmp = 0;
		String s = "";
		ArrayList<HashMap.Entry<Integer, Integer>> list_Data = new ArrayList<HashMap.Entry<Integer, Integer>>(FreqFHash.entrySet());
		ArrayList<HashMap.Entry<Integer, Integer>> list_HitFile = new ArrayList<HashMap.Entry<Integer, Integer>>(HitHash.entrySet());
		File source = new File(dataPath);
		File[ ] file = source.listFiles();
		
		// File 被 hit 的次數
		Collections.sort( list_HitFile, new Comparator<HashMap.Entry<Integer, Integer>>( ){
			public int compare(HashMap.Entry<Integer, Integer> entry1,HashMap.Entry<Integer, Integer> entry2){
				return (entry2.getValue().compareTo(entry1.getValue()) );
			}
		} );
		
		for(HashMap.Entry<Integer, Integer> entry:list_HitFile){
			if(Hittmp >1 ){
				if(entry.getValue() != Hittmp || Hittmp == queryNum){
					System.out.println(s + "hello" + queryNum);
					listFile += s+",";
					System.out.println("hit file more than 1 : " + s + Hittmp );
				}
				else
					break;
			}
			Hittmp = entry.getValue();
			s = file[ entry.getKey() ].getName();
		}
		// word 出現的頻率做排序 
		Collections.sort( list_Data, new Comparator<HashMap.Entry<Integer, Integer>>( ){
			public int compare(HashMap.Entry<Integer, Integer> entry1,HashMap.Entry<Integer, Integer> entry2){
				return (entry2.getKey().compareTo( entry1.getKey() ) );
			}
		} );
		for(HashMap.Entry<Integer, Integer> entry:list_Data){
			listFile += file[ entry.getValue() ].getName()+",";
		}
	}
	
	/* Show the file which query hits from more to less on the Frame , it will show top-20 results for each query */
	public void ShowHitFile( ) {
		DLM.removeAllElements();
		
		if(listFile == ""){
			NotF.setVisible(true);
			listbox.setVisible(false);
		}
		else{
			NotF.setVisible(false);
			listbox.setVisible(true);
			String[ ] LF = listFile.split(",");
			ArrayList<String> FileList = new ArrayList<String>( );
			
			for(int i = 0 ; i<LF.length ; i++){
				if( !FileList.contains( LF[i] ) ){
					FileList.add(LF[i]);
					DLM.addElement( LF[i] );
					
					if(DLM.getSize() > 20)
						return;
				}							 
			}
		}
	}
	
	
}
