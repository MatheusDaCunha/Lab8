class RecursoCompartrilhado {
	private int variavel;

	public RecursoCompartrilhado () {
		this.variavel = 0;
	}

	public void incrementar () {
		this.variavel++;
	}

	public int getRecursoCompartrilhado () {
		return this.variavel;
	}

	public void setRecursoCompartrilhado (int valor) {
        this.variavel = valor;
	}
}

class LE {
  private int leit, escr;

  // Construtor
  LE() {
     this.leit = 0; //leitores lendo (0 ou mais)
     this.escr = 0; //escritor escrevendo (0 ou 1)
  }

  // Entrada para leitores
  public synchronized void entraLeitor (int id) {
    try {
      while (this.escr > 0) {
      //if (this.escr > 0) {
         System.out.println ("le.leitorBloqueado("+id+")");
         wait();  //bloqueia pela condicao logica da aplicacao
      }
      this.leit++;  //registra que ha mais um leitor lendo
      System.out.println ("le.leitorLendo("+id+")");
    } catch (InterruptedException e) { }
  }

  // Saida para leitores
  public synchronized void saiLeitor (int id) {
     this.leit--; //registra que um leitor saiu
     if (this.leit == 0)
           this.notify(); //libera escritor (caso exista escritor bloqueado)
     System.out.println ("le.leitorSaindo("+id+")");
  }

  // Entrada para escritores
  public synchronized void entraEscritor (int id) {
    try {
      while ((this.leit > 0) || (this.escr > 0)) {
      //if ((this.leit > 0) || (this.escr > 0)) {
         System.out.println ("le.escritorBloqueado("+id+")");
         wait();  //bloqueia pela condicao logica da aplicacao
      }
      this.escr++; //registra que ha um escritor escrevendo
      System.out.println ("le.escritorEscrevendo("+id+")");
    } catch (InterruptedException e) { }
  }

  // Saida para escritores
  public synchronized void saiEscritor (int id) {
     this.escr--; //registra que o escritor saiu
     notifyAll(); //libera leitores e escritores (caso existam leitores ou escritores bloqueados)
     System.out.println ("le.escritorSaindo("+id+")");
  }
}

class TarefaDasThreads_1 implements Runnable{
    private LE monitor;
    private int id;
    private RecursoCompartrilhado variavel;

    public TarefaDasThreads_1(LE monitor, int id, RecursoCompartrilhado variavel){
        this.monitor = monitor;
        this.id = id;
        this.variavel = variavel;
    }
    public void run() {
        //System.out.println("**** Thread " + id + " COMEÇOU ****");
        this.monitor.entraEscritor(id);
        System.out.println("---- Thread " + id + ": Vai Incrementar a variavel");
		variavel.incrementar();
		this.monitor.saiEscritor(id);
		//System.out.println("**** Thread " + id + " TERMINOU ****");
	}
}

class TarefaDasThreads_2 implements Runnable{
    private LE monitor;
    private int id;
    RecursoCompartrilhado variavel;

    public TarefaDasThreads_2(LE monitor, int id, RecursoCompartrilhado variavel){
        this.monitor = monitor;
        this.id = id;
        this.variavel = variavel;
    }
    public void run() {
        //System.out.println("**** Thread " + id + " COMEÇOU ****");
        this.monitor.entraLeitor(id);
		if(variavel.getRecursoCompartrilhado() % 2 == 0){
            System.out.println("---- Thread " + id + ": " + variavel.getRecursoCompartrilhado() + " é um número PAR");
		}
		else{
            System.out.println("---- Thread " + id + ": " + variavel.getRecursoCompartrilhado() + " é um número ÍMPAR");
		}
		this.monitor.saiLeitor(id);
		//System.out.println("**** Thread " + id + " TERMINOU ****");
	}

}

class TarefaDasThreads_3 implements Runnable{
    private LE monitor;
    private int id, processa;
    RecursoCompartrilhado variavel;

    public TarefaDasThreads_3(LE monitor, int id, RecursoCompartrilhado variavel){
        this.monitor = monitor;
        this.id = id;
        this.processa = 0;
        this.variavel = variavel;
    }
    public void run() {
        //System.out.println("**** Thread " + id + " COMEÇOU ****");
        this.monitor.entraLeitor(id);
        System.out.println("---- Thread " + id + ": valor da variável é " + variavel.getRecursoCompartrilhado());
        this.monitor.saiLeitor(id);

        for(int i = 0; i < 1000000; i++){
            this.processa++;
        }

        this.monitor.entraEscritor(id);
        variavel.setRecursoCompartrilhado(id);
		this.monitor.saiEscritor(id);
		//System.out.println("**** Thread " + id + " TERMINOU ****");
	}
}

class Principal {
	static final int nThreads_1 = 2;
	static final int nThreads_2 = 2;
	static final int nThreads_3 = 2;

	public static void main(String[] args) {
		LE monitor = new LE();
		Thread[] threadsTipo1 = new Thread[nThreads_1];
		Thread[] threadsTipo2 = new Thread[nThreads_2];
		Thread[] threadsTipo3 = new Thread[nThreads_3];
		RecursoCompartrilhado recurso = new RecursoCompartrilhado();
		int id;

        /* Cria os 3 tipos de threads */
		for (int i = 0; i < nThreads_1; i++) {
			id = i;
			threadsTipo1[i] = new Thread(new TarefaDasThreads_1(monitor, id, recurso));
		}
		for (int i = 0; i < nThreads_2; i++) {
			id = i + nThreads_1;
			threadsTipo2[i] = new Thread(new TarefaDasThreads_2(monitor, id, recurso));
		}
		for (int i = 0; i < nThreads_3; i++) {
			id = i + nThreads_1 + nThreads_2;
			threadsTipo3[i] = new Thread(new TarefaDasThreads_3(monitor, id, recurso));
		}

        /* Inicia a execução das threads */
        for (int i = 0; i < nThreads_1; i++) {
			threadsTipo1[i].start();
		}
		for (int i = 0; i < nThreads_2; i++) {
			threadsTipo2[i].start();
		}
		for (int i = 0; i < nThreads_3; i++) {
			threadsTipo3[i].start();
		}

        /* Aguarda o témino correto de todas as threads */
		for (int i  = 0; i < nThreads_1; i++) {
			try {
				threadsTipo1[i].join();
			}
			catch (InterruptedException e) {
				return;
			}
		}
		for (int i  = 0; i < nThreads_2; i++) {
			try {
				threadsTipo2[i].join();
			}
			catch (InterruptedException e) {
				return;
			}
		}
		for (int i  = 0; i < nThreads_3; i++) {
			try {
				threadsTipo3[i].join();
			}
			catch (InterruptedException e) {
				return;
			}
		}

		System.out.println("\nO valor final da variavel é " + recurso.getRecursoCompartrilhado());
		return;
	}
}
