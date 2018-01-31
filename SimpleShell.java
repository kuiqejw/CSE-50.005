
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class SimpleShell {
	public static void main(String[] args) throws java.io.IOException{
		String commandLine;
		ArrayList<String> historyIndex = new ArrayList<String>();
		ArrayList<String> command;
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		String line;
		File file;
		File currentDir = new File(System.getProperty("user.dir"));
		File newpath;
		ArrayList<ArrayList<String>> history = new ArrayList<ArrayList<String>>();
		//break out with ctl c

		//TODO: creating the external process and executing the command in that process
		
		
		while(true){
			//read what the user entered
			System.out.print("jsh>");
			commandLine = console.readLine();
			command = new ArrayList<String>(Arrays.asList(commandLine.split(" ")));

			//TODO: ADDING A HISTORY FEATURE
			//if the user entered a return, just loop again
			if (commandLine.equals("")){
				continue;
			}
			
			//change directory
			else if (command.get(0).equals("cd")){
				history.add(0,command);
				historyIndex.add(Integer.toString(history.size()-1));
				if (commandLine.equals("cd")){
					newpath = new File(System.getProperty("user.home"));
				}else if (command.get(1).equals("..")){
					file = new File(System.getProperty("user.dir"));
					newpath = new File(file.getParent());//to use file as a temporary storage holder for the old path
				}else{
					newpath = new File(currentDir.getAbsolutePath()+File.separator+command.get(1));
				}
				if (newpath.isDirectory()){
					currentDir = newpath;
				}else{
					System.out.println("Directory not exist" + newpath);
				}continue;
			}
			//Qn 3 : history component:
			else if (command.get(0).trim().toLowerCase().equals("history")){
				for(int i = 0; i < history.size(); i++) {
					System.out.print(i+1);
					System.out.print(" ");
					System.out.println(String.join(" ", history.get(i)));
				
				}
				continue;
				//call command from last called
			}else if (commandLine.matches("!!")){
				if (history.size() == 0){
					System.out.println("No previous command found");
					continue;
				}else {
					command = history.get(history.size()-1);
					//System.out.print("got pwd");
				}}
				//call command from history index
			else if (historyIndex.contains(commandLine)){
				command = history.get(Integer.parseInt(commandLine)-1);
				}
			else if (commandLine.matches("-?\\d+")){
				if (Integer.parseInt(commandLine)>historyIndex.size()){
					System.out.println("The number must be between 1 and "+ (historyIndex.size()));
					continue;
				}}
			else{
				//store history
				history.add(0,command);
				historyIndex.add(Integer.toString(history.size()-1));
				
			}
			
			try{
				
				ProcessBuilder pb = new ProcessBuilder(); 
				pb.command(command); 
				pb.directory(currentDir);
				Process p = pb.start();
				InputStream is = p.getInputStream();  
				InputStreamReader isr = new InputStreamReader(is);
        		BufferedReader br = new BufferedReader(isr);
        
        		while ((line = br.readLine()) != null) {
            		System.out.println(line);
        		}
        	} catch (IOException e) {
				System.out.println(e.getMessage());
        	}
		}
	}
}