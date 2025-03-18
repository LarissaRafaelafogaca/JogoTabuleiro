package jogo.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class JogoDaVelhaComDado {
    private JFrame frame;
    private JButton[][] botoes;
    private JButton iniciarJogo, rolarDado, informarPosicoes;
    private JLabel resultadoDado;
    private static final int TAMANHO = 3;
    private int posicaoJogador;
    private Random random;
    private char jogadorAtual;
    private boolean jogoAtivo;
    private FireworksPanel fireworksPanel;

    public JogoDaVelhaComDado() {
        frame = new JFrame("Jogo da Velha com Dado");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.setLayout(new BorderLayout());

        random = new Random();
        jogadorAtual = 'X';  
        posicaoJogador = 0;
        jogoAtivo = true;

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(4, 1));
        menuPanel.setBackground(new Color(70, 130, 180)); 

        iniciarJogo = new JButton("1 - Iniciar Nova Partida");
        rolarDado = new JButton("2 - Executar Jogada");
        informarPosicoes = new JButton("3 - Informar Posições");
        JButton sair = new JButton("0 - Sair");
        resultadoDado = new JLabel("Role o dado para começar", SwingConstants.CENTER);
        resultadoDado.setForeground(new Color(0, 0, 128)); 

        
        configurarBotao(iniciarJogo);
        configurarBotao(rolarDado);
        configurarBotao(informarPosicoes);
        configurarBotao(sair);

        
        iniciarJogo.addActionListener(e -> iniciarNovaPartida());
        rolarDado.addActionListener(e -> rolarDado());
        informarPosicoes.addActionListener(e -> informarPosicoes());
        sair.addActionListener(e -> System.exit(0));

        
        menuPanel.add(iniciarJogo);
        menuPanel.add(rolarDado);
        menuPanel.add(informarPosicoes);
        menuPanel.add(sair);

        
        frame.add(menuPanel, BorderLayout.NORTH);
        frame.add(resultadoDado, BorderLayout.CENTER);

      
        JPanel tabuleiroPanel = new JPanel(new GridLayout(TAMANHO, TAMANHO));
        tabuleiroPanel.setBackground(new Color(255, 255, 255)); 
        botoes = new JButton[TAMANHO][TAMANHO];
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                botoes[i][j] = new JButton("-");
                botoes[i][j].setFont(new Font("Arial", Font.BOLD, 40));
                botoes[i][j].setEnabled(false); 
                botoes[i][j].setBackground(new Color(240, 248, 255)); 
                botoes[i][j].setForeground(new Color(0, 0, 128)); 
                tabuleiroPanel.add(botoes[i][j]);
            }
        }
        frame.add(tabuleiroPanel, BorderLayout.CENTER);

        fireworksPanel = new FireworksPanel();
        frame.add(fireworksPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void configurarBotao(JButton botao) {
        botao.setBackground(new Color(70, 130, 180)); 
        botao.setFont(new Font("Arial", Font.BOLD, 16));
        botao.setForeground(Color.BLACK); 
        botao.setFocusPainted(false); 

      
        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botao.setBackground(new Color(100, 149, 237)); 
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botao.setBackground(new Color(70, 130, 180)); 
            }
        });
    }

    private void iniciarNovaPartida() {
        posicaoJogador = 0;
        jogadorAtual = 'X';  
        jogoAtivo = true;
        resultadoDado.setText("Nova partida iniciada! Role o dado.");
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                botoes[i][j].setText("-");
                botoes[i][j].setEnabled(true); 
            }
        }
    }

    private void rolarDado() {
        if (!jogoAtivo) {
            resultadoDado.setText("O jogo terminou! Inicie uma nova partida.");
            return;
        }

        int valorDado = random.nextInt(9) + 1; 
        resultadoDado.setText("Dado: " + valorDado);

        
        int linha = (valorDado - 1) / TAMANHO;
        int coluna = (valorDado - 1) % TAMANHO;

      
        if (botoes[linha][coluna].getText().equals("-")) {
            botoes[linha][coluna].setText(String.valueOf(jogadorAtual));
            if (verificarVitoria()) {
                exibirMensagemVitoria(); 
                jogoAtivo = false;
                return;
            }
           
            jogadorAtual = (jogadorAtual == 'X') ? 'O' : 'X';
        } else {
            resultadoDado.setText("Célula já ocupada! Role novamente.");
        }
    }

    private void exibirMensagemVitoria() {
        
        JOptionPane.showMessageDialog(frame, "Jogador " + jogadorAtual + " venceu!", "Vitória!", JOptionPane.INFORMATION_MESSAGE);

        
        fireworksPanel.iniciarFogos();
    }

    private void informarPosicoes() {
        StringBuilder posicoes = new StringBuilder("Posições no tabuleiro:\n");
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                posicoes.append("[").append(i).append("][").append(j).append("] -> ")
                         .append(botoes[i][j].getText()).append("\n");
            }
        }
        JOptionPane.showMessageDialog(frame, posicoes.toString(), "Posições", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean verificarVitoria() {
        
        for (int i = 0; i < TAMANHO; i++) {
            if (botoes[i][0].getText().equals(String.valueOf(jogadorAtual)) &&
                botoes[i][1].getText().equals(String.valueOf(jogadorAtual)) &&
                botoes[i][2].getText().equals(String.valueOf(jogadorAtual))) {
                return true;
            }
            if (botoes[0][i].getText().equals(String.valueOf(jogadorAtual)) &&
                botoes[1][i].getText().equals(String.valueOf(jogadorAtual)) &&
                botoes[2][i].getText().equals(String.valueOf(jogadorAtual))) {
                return true;
            }
        }

        if (botoes[0][0].getText().equals(String.valueOf(jogadorAtual)) &&
            botoes[1][1].getText().equals(String.valueOf(jogadorAtual)) &&
            botoes[2][2].getText().equals(String.valueOf(jogadorAtual))) {
            return true;
        }

        if (botoes[0][2].getText().equals(String.valueOf(jogadorAtual)) &&
            botoes[1][1].getText().equals(String.valueOf(jogadorAtual)) &&
            botoes[2][0].getText().equals(String.valueOf(jogadorAtual))) {
            return true;
        }

        return false;
    }

   
    class FireworksPanel extends JPanel {
        private Timer timer;
        private int ciclos;
        private boolean fogosAtivos;

        public FireworksPanel() {
            setPreferredSize(new Dimension(400, 100));
            setBackground(Color.BLACK);
            ciclos = 0;
            fogosAtivos = false;

            timer = new Timer(100, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (fogosAtivos) {
                        ciclos++;
                        if (ciclos > 20) {
                            fogosAtivos = false;
                            setBackground(Color.BLACK); 
                            ciclos = 0;
                        } else {
                            int r = random.nextInt(255);
                            int g = random.nextInt(255);
                            int b = random.nextInt(255);
                            setBackground(new Color(r, g, b));
                            repaint();
                        }
                    }
                }
            });
        }

        public void iniciarFogos() {
            fogosAtivos = true;
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (fogosAtivos) {
                
                int x = random.nextInt(getWidth());
                int y = random.nextInt(getHeight());
                for (int i = 0; i < 12; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    int dx = (int) (Math.cos(angle) * 60);
                    int dy = (int) (Math.sin(angle) * 60);
                    g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
                    g.drawLine(x, y, x + dx, y + dy);
                }
            }
        }
    }

    public static void main(String[] args) {
        new JogoDaVelhaComDado(); 
    }
}
