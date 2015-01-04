//Elaborado por:
//Alexandre Rui Santos Fonseca Pinto 2010131853
//Carlos Miguel Rosa Avim 2000104864
//user: admin   pass: irc2011

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;


public class NATServer {
	
	// Possui os enderecos publicos adicionados pelo administrador.
	static ArrayList<String> enderecosPublic = new ArrayList<String>();
	
	// Possui o tamanho igual ao numero de enderecos publico. Possui o ultimo porto atribuido para cada endereco.
	static ArrayList<Integer> portos = new ArrayList<Integer>();
	
	// Possui os enderecos privados dos clientes a quem foi atribuido um endereco publico utilizando NAT estatico.
	static ArrayList<String> clientes = new ArrayList<String>();
	
	// Possui os enderecos e portos atribuidos utilizando NAT estatico.
	static ArrayList<String> enderecAtribuidos = new ArrayList<String>();
	
	static int nat=1,pass=2055295032,user=92668751; // dados guardados de maneira segura usando hashing
	static File log;								//representacao logica do ficheiro de log
	static PrintWriter logWrite;					//Classes de escrita em ficheiro
	static FileWriter fWrite;
	static BufferedWriter buffWrite;
	
	static Semaphore mutex;							//semaforo para controlar o acesso exclusivo aos recursos partilhados
	
	public static void main(String[] args) throws IOException {
	
		ServerSocket servidor = null; // socket para o servidor aguardar por novas coneccoes
		Socket cliente = null; // socket para cada cliente
		
		mutex = new Semaphore(1, true);				//inicializacao do semaforo
	
		try {										//abertura do novo ficheiro para escrita
			 log=new File("log.txt");
			 fWrite=new FileWriter(log,true);
			 buffWrite=new BufferedWriter(fWrite);
			 logWrite=new PrintWriter(buffWrite); 
			
		} catch (IOException e) {
			System.out.println("Ocorreu uma excepcao " + e + " ao criar o ficheiro de log");
		}
		
		if(args.length != 1) {									//mensagem de erro caso o porto nao seja inserido
			System.out.println("Uso: NatServer <porto>");
			return;
		}
		
		try {
			servidor = new ServerSocket(Integer.parseInt(args[0]));			//cria um socket servidor ,num porto passado como argumento pela linha de comandos
			logWrite.println("[ "+new Date()+" ] Servidor iniciado.");		//escreve em ficheiro a data em que o servidor foi criado e iniciado
		} catch (IOException e) {
			System.out.println("Erro ao criar o socket do servidor.");
			logWrite.println("[ "+new Date()+" ] Erro ao iniciar servidor.");
			logWrite.close();
			return;
		}
	
		while(true){
			try {
				try {
					 log=new File("log.txt");							
					 fWrite=new FileWriter(log,true);
					 buffWrite=new BufferedWriter(fWrite);
					 logWrite=new PrintWriter(buffWrite); 
					
				} catch (IOException e) {
					System.out.println("Ocorreu uma excepcao " + e + " ao criar o ficheiro de log");
				}
				
			
				cliente= servidor.accept();				// aceita uma nova coneccao vinda do cliente e conecta-a ao socket
				
				logWrite.println("[ "+new Date() +" ] O cliente: "+cliente.getInetAddress().toString() + " conectou-se ao servidor.");		//regista no log file
				System.out.println("O cliente: "+cliente.getInetAddress().toString() + " conectou-se ao servidor.");
				
			
				ClienteThread novoCliente = new ClienteThread(cliente);				// cria uma nova thread para gerir o novo cliente
				novoCliente.start(); 												// inicia a thread
				
				
			}catch (IOException e) {
					System.out.println("Erro ao tratar novo cliente.");
			}
		}
	}
	
	static class ClienteThread extends Thread{
		
		Socket cliente; // Socket para o novo cliente
		Scanner input; // scanner para ler dados do socket do cliente
		PrintWriter output; // Para escrever dados para o socket
		
		
		public ClienteThread(Socket cliente){

			this.cliente = cliente;
			
			try {
				this.input = new Scanner(cliente.getInputStream());						//cria um Scanner para ler dados do Socket
				this.output = new PrintWriter(cliente.getOutputStream(), true);			//cria um PrintWriter para ler dados para o socket
				
			} catch (IOException e) {
				System.out.println("Erro a criar streams de dados.");
			}
		}
		
		
		public void run(){
			
			String opcao=null;
			limpaEcra();
			while (enderecosPublic.size() == 0) {				//enquanto nao forem introduzidos enderecos publicos pelo administrador, ou opcao invalida
																//mostra o menu
				do {
					output.println("\n\nEscolha a opcao pretendida:");
					output.println("	[1] - Entrar como admnistrador.");
					output.println("	[0] - Sair.");
					try {
					opcao = input.next();						//le opcao
					} 
					catch (NoSuchElementException e1) { break; }
					catch (IllegalStateException e2) { break; }

				} while(!opcao.equalsIgnoreCase("1") && !opcao.equalsIgnoreCase("0"));

				if (opcao.equalsIgnoreCase("1")) {
					try {
						admin();						//executa funcao de administrador
					} catch (IOException e) {
						output.println("Erro a criar ficheiro de log: "+e);
					}
					catch (NoSuchElementException e1) { break; }
					catch (IllegalStateException e2) { break; }
				}
				
				else {
					terminar();					//encerra o cliente
					return;
				}
			}
			
			while (true) {

				do {															//mostra menu
					output.println("\n\nEscolha a opcao pretendida:");
					output.println("	[1] - Entrar como admnistrador.");
					if(nat==1)																	//caso o servidor esteja a correr em NAt dinamico
						output.println("	[2] - Traduzir endereco usando NAT Dinanimo.");
					else if (nat==2)															//caso o servidor esteja a correr em NAt estatico
						output.println("	[2] - Traduzir endereco usando NAT Estatico.");
					output.println("	[3] - Estabelecer ligacao para um endereco de destino.");
					output.println("	[0] - Sair.");
					try {
						opcao = input.next();			//le opcao
						} 
						catch (NoSuchElementException e1) { break; }
						catch (IllegalStateException e2) { break; }
				} while(!opcao.equalsIgnoreCase("1") && !opcao.equalsIgnoreCase("2") && !opcao.equalsIgnoreCase("3") && !opcao.equalsIgnoreCase("0"));;

				if (opcao.equalsIgnoreCase("1")) {
					try {
						admin();						//executa funcao de administrador
					} catch (IOException e) {
						output.println("Erro ao executar funcao administrador: "+e);
					}
					catch (NoSuchElementException e1) { break; }
					catch (IllegalStateException e2) { break; }
					catch (java.lang.NullPointerException e3)  { break; }
					return;
				}
				else if (opcao.equalsIgnoreCase("2") && nat==1) {
					natDin();							//executa funcao de NAT dinamico
					terminar();							//encerra o cliente
					return;
				}
				else if (opcao.equalsIgnoreCase("2") && nat==2) {
					natEst();							//executa funcao de NAT estatico
					terminar();							//encerra o cliente
					return;
				}
				else if (opcao.equalsIgnoreCase("3")){
					link();								//executa funcao de estabelecer ligacao
					terminar();							//encerra o cliente
					return;
				}
				else {
					terminar();							//encerra o cliente
					break;
				}
			}
		}
			

		private void link() {
			
			Socket servidorDest = null;				//socket para o endereco destino
			Scanner input2=null;					//Scanner para ler do socket
			PrintWriter output2=null; 				//PrintWriter para escrever no socket
			
			String host=null,porto=null,recebido=null;				//nome do endereco destino,porto e mensagem recebida
			limpaEcra();
			
			try {
				output.println("Introduza o nome do endereco destino a qual se pretende ligar: ");
				host=input.next();									//pede endereco de destino ao cliente
				} 
			catch (NoSuchElementException e1) {}
			catch (IllegalStateException e2) {}
			
			
			
			try {
				output.println("Introduza o porto do endereco pretendido: ");
				porto=input.next();								//pede porto
				} 
			catch (NoSuchElementException e1) {}
			catch (IllegalStateException e2) {}
			
			
			
			try {
				servidorDest = new Socket(host,Integer.parseInt(porto));				//cria um socket com o endereco destino
			} 
			catch (IOException e) {
				output.println("Erro ao criar o socket para o servidor destino.");
				return;
			}
			catch(java.lang.NumberFormatException e){}
			
			try {
				input2 = new Scanner(servidorDest.getInputStream());					//cria Scanner
				output2 = new PrintWriter(servidorDest.getOutputStream(), true);		//cria PrintWriter  
				
			} 
			catch (IOException e) {
				System.out.println("Erro a criar streams de dados.");
			}
			catch(java.lang.NullPointerException e){}
			
			
			output.println("\n\nA ligacao ao endereco "+host+" no porto "+porto+" foi estabelecida com sucesso\n\n");
			
			logWrite.println("[ "+new Date()+" ]cliente estabeleceu a ligacao com o endereco "+host+" na porta "+porto+": "		//regista accao em logfile
					+cliente.getInetAddress().getHostAddress()+".");
			
			while(true)
			{
				output.println("(-1 para sair)\n>");
				try{
					if((input.next()).equalsIgnoreCase("-1"))				//le dados do cliente
					{
							return;
					}
					
					output2.println(recebido);				//envia dados do cliente para o endereco destino
					
						while(input2.hasNext()){			//recebe,enquanto houver,  dados do  destino 
							recebido=input2.next();
							recebido+=input2.nextLine();
							output.println(recebido);		//volta a enviar de volta dados para o cliente inicial
						}
				}
				catch (NoSuchElementException e1) {}
				catch (IllegalStateException e2) {}
			}	
		}

// Politica de NAT estatico
		private void natEst() {
			
			InetAddress enderecoCliente = cliente.getInetAddress(); //obtem o endereco privado do cliente
			int numEnder = 0;
			
			if (!verificaPrivado(enderecoCliente)) {               // verifica se endereco do cliente e' valido
				output.println( "127.0.0.1" );
				logWrite.println("[ "+new Date()+" ] Cliente tentou obter endereco publico de um endereco privado nao valido: "
												+ enderecoCliente.getHostAddress()+".");    // regista no log
			}
			else {
				for(String cliente : clientes)
					if(cliente.equalsIgnoreCase(enderecoCliente.getHostAddress())) {      // verifica se anteriormente foi fornecido um endereco publico a esse cliente
						output.println( enderecAtribuidos.get(clientes.indexOf(cliente)) );
						logWrite.println("[ "+new Date()+" ] Atribuido o endereco publico: "+enderecAtribuidos.get(clientes.indexOf(cliente)) +" ao cliente: "
						 																	+enderecoCliente.getHostAddress()+".");
						return;
					}

				numEnder = enderecosPublic.size();
				Random gera = new Random();
				int escolhido = gera.nextInt(numEnder);						// gera um numero para fornecer aleatoriamente um endereco publico disponivel na pool

				try {
					mutex.acquire();
				} catch (InterruptedException e) {
					output.println("Ocorreu um erro: "+e);
				}
				portos.set(escolhido, portos.get(escolhido)+1 );			// incrementa o porto
				clientes.add(enderecoCliente.getHostAddress());										// adiciona o endereco do cliente 'a lista de clientes
				enderecAtribuidos.add(enderecosPublic.get(escolhido)+":"+portos.get(escolhido));	// adiciona o endereco atribuido 'a lista
				
				output.println( enderecAtribuidos.get(enderecAtribuidos.size()-1) );				// devolve ao cliente este ultimo endereco adicionado
				mutex.release();
				logWrite.println("[ "+new Date()+" ] Atribuido o endereco publico: "+enderecAtribuidos.get(enderecAtribuidos.size()-1) +" ao cliente: " 
																					+ enderecoCliente.getHostAddress()+".");
			}
		}

// Politica de NAT dinamica
		private void natDin() {
			
			InetAddress enderecoCliente = cliente.getInetAddress();    //obtem o endereco privado do cliente
			int numEnder=0;
			
			if (!verificaPrivado(enderecoCliente)) {               // verifica se endereco do cliente e' valido
				output.println( "127.0.0.1" );
				logWrite.println("[ "+new Date()+" ] Cliente tentou obter endereco publico de um endereco privado nao valido: "
												+ enderecoCliente.getHostAddress()+".");
			}
			
			else {
				numEnder = enderecosPublic.size();
				Random gera = new Random();
				int escolhido = gera.nextInt(numEnder);      // gera um numero para fornecer aleatoriamente um endereco publico disponivel na pool

				try {
					mutex.acquire();
				} catch (InterruptedException e) {
					output.println("Ocorreu um erro: "+e);
				}
				portos.set(escolhido, portos.get(escolhido)+1 );     // incrementa o porto
				
				output.println( enderecosPublic.get(escolhido)+":"+portos.get(escolhido) );  // devolve ao cliente o endereco publico e o porto
				mutex.release();
				logWrite.println("[ "+new Date()+" ] Atribuido o endereco publico: "+enderecosPublic.get(escolhido)+":"+portos.get(escolhido) +" ao cliente: " 
																					+enderecoCliente.getHostAddress()+".");
			}
		}


		private void admin() throws IOException {
			
			String opcao=null,utilizador,senha;
			int tentativas=0;
			
			do{											//no maximo 3 tentativas, pede login e pass
				if(tentativas>0 && tentativas <3)
					output.println("\nO utilizador ou a password estao incorrectas, restam "+(3-tentativas)+" tentativas");
				else if(tentativas==3){
					output.println("A ligacao foi encerrada, por excesso de tentativas");
					logWrite.println("[ "+new Date()+" ] Administrador tentou entrar nos sistema mas falhou as 3 tentativas: "
													+ cliente.getInetAddress().getHostAddress()+".");
					terminar();
					return;
				}
				output.println("Login: ");
				utilizador= input.next();			//pede login
				output.println("\nPassword: ");
				senha= input.next();				//pede pass
				tentativas++;
			}while(utilizador.hashCode()!=user || senha.hashCode()!=pass);		//valida ou nao entrada no servidor
			
			logWrite.println("[ "+new Date()+" ] Administrador entrou no sistema com sucesso: "+ cliente.getInetAddress().getHostAddress()+".");
															//regista o evento
			limpaEcra();
			while(true)
			{
				do {
					if(enderecosPublic.size() == 0)								//imprime nota se faltar acrescentar enderecos publicos
						output.println("\n\nNota: Falta adicionar enderecos publicos a pool.");
					if(nat==1)								//imprime a politica por defeito se esta estiver activa
						output.println("\n(Por defeito,o servidor esta a usar politicas de NAT dinamico)");
					output.println("\n\nEscolha a opcao pretendida:");		//imprime menu
					output.println("	[1] - Adicionar enderecos publicos a pool.");
					output.println("	[2] - Definir politicas de NAT dinamico ou estatico.");
					output.println("	[3] - Visualizar enderecos publicos inseridos .");
					output.println("	[4] - Visualizar ficheiro de log.");
					output.println("	[5] - Apagar ficheiro de log.");
					output.println("	[0] - Sair.");
					try {
						opcao = input.next();		//le opcao
						} 
						catch (NoSuchElementException e1) {break;}
						catch (IllegalStateException e2) {break;}
						
				} while(!opcao.equalsIgnoreCase("1") && !opcao.equalsIgnoreCase("2") && !opcao.equalsIgnoreCase("3") && !opcao.equalsIgnoreCase("4")
				 									 && !opcao.equalsIgnoreCase("5") &&!opcao.equalsIgnoreCase("0"));
			
		
			
				if (opcao.equalsIgnoreCase("1"))
					adicionaEnderecos();				//executa funcao de adicionar enderecos
				else if (opcao.equalsIgnoreCase("2"))
					estabelecePolitica();				//executa funcao de estabelecer politica de NAT
				else if (opcao.equalsIgnoreCase("3"))
					visualizaPublicos();				//executa funcao de visualizar enderecos publicos 
				else if (opcao.equalsIgnoreCase("4"))
					visualizaLog();						//executa funcao de visualizar ficheiro de log
				else if (opcao.equalsIgnoreCase("5"))	
					apagaLog();							//executa funcao de apagar o ficheiro
				else {
					terminar();							// encerra cliente
					break;
				}
			}
		}
		
		private void adicionaEnderecos()
		{
			String opcao=null;
			
			limpaEcra();
			do{
				output.println("\n\nIntroduza o endereco publico a adicionar a pool: (-1 para sair)");		
				opcao = input.next();												//pede endereco a adicionar
				if(opcao.equalsIgnoreCase("-1"))
					return;
				else{
					enderecosPublic.add(opcao);
					portos.add(1024);												//valor inicial do porto 1024
					output.println("Endereco '"+opcao+"' adicionado");
					logWrite.println("[ "+new Date()+" ] Administrador adicionou o endereco publico "+opcao+" a pool: "			//regista o facto
													+ cliente.getInetAddress().getHostAddress()+".");
				}
				
			}while(!opcao.equalsIgnoreCase("-1"));
		}
		
		private void estabelecePolitica()
		{
			String opcao=null;
			limpaEcra();
			do{																	//imprime menu
				output.println("\n\nQual a politica que pretende definir?:");
				output.println("	[1] - NAT Dinamico(por defeito).");
				output.println("	[2] - NAT Estatico.");
				output.println("	[0] - Voltar.");
				opcao = input.next();
			}while(!opcao.equalsIgnoreCase("1") && !opcao.equalsIgnoreCase("2")  && !opcao.equalsIgnoreCase("0"));
			
			if (opcao.equalsIgnoreCase("1")){ 
				nat=1;					//estabelece politica de NAT dinamico
				output.println("A politica de NAT dinamico foi estabelecida");
				logWrite.println("[ "+new Date()+" ] Administrador estabeleceu a politica de NAT dinamico: "		//regista o facto
												+ cliente.getInetAddress().getHostAddress()+".");
			}
			else if (opcao.equalsIgnoreCase("2")){
				nat=2;					//estabelece politica de NAT estatico
				output.println("A politica de NAT estatico foi estabelecida");
				logWrite.println("[ "+new Date()+" ] Administrador estabeleceu a politica de NAT estatico: "		//regista o facto
												+cliente.getInetAddress().getHostAddress()+".");
			}
			else
				return;
		}
		
		private void visualizaPublicos()
		{
			limpaEcra();
			output.println("Enderecos publicos:\n");
			for(String publico: enderecosPublic)			//percorre array de enderecos e imprime no ecra do cliente
				output.println("->"+publico);
		
		}
		
		private void visualizaLog()
		{
			String linha;
			limpaEcra();
			
			logWrite.flush();
			try{
				BufferedReader br=new BufferedReader(new FileReader(log));				//cria leitor do ficheiro
				while((linha=br.readLine())!=null)										//le linhas enquanto nao chegar ao fim do ficheiro
				{
					output.println(linha);												//imprime linhas no ecra do cliente
				}
			}
			catch (FileNotFoundException e) {
				output.println("Ficheiro inexistente");
			}
			catch (IOException e) {
				output.println("Erro ao ler o ficheiro");
			}
		}
		
		private void apagaLog() throws IOException
		{
			log.delete();						//apaga ficheiro de log
			log = new File("log.txt");			//cria novo ficheiro
			logWrite = new PrintWriter(new BufferedWriter (new FileWriter(log, true)));				
			
			logWrite.println("-------------------------- FICHEIRO DE LOG --------------------------");
		}
		
		private void terminar() {
			
			input.close();
			output.close();
			try {
				cliente.close();		//fecha socket do cliente
				logWrite.flush();		//actualiza o ficheiro de log
				logWrite.close();		//fecha escritor do ficheiro
				
			} catch (IOException e) {
				System.out.println("Error while closing the socket.");
			}

		}
		
		private void limpaEcra(){
			for(int i=0;i<20;i++)			//limpa ecra, para melhor visualizacao
				output.println("\n");
		}
		
		private boolean verificaPrivado(InetAddress endereco) {
			
			int gama;
			boolean compara = false;
			
			if(endereco.getHostAddress().substring(0, endereco.getHostAddress().indexOf(".")).equalsIgnoreCase("10")) //verifica se comeca por 10
				compara=true;
			
			else if(endereco.getHostAddress().substring(0,7).equalsIgnoreCase("192.168"))							 //verifica se comeca por 192.168
				compara=true;
			
			else if(endereco.getHostAddress().substring(0,3).equalsIgnoreCase("172")) {								//verifica se comeca por 172
				gama = Integer.parseInt(endereco.getHostAddress().substring(4,6));
				if (gama >= 16 && gama <=31)														 //verifica se estao dentro da gama
					compara=true;
			}
				
			return compara;
			
		}
	}
}
	
