
# Simplex em Duas Fases — Java

Implementação do Método Simplex em Duas Fases para resolver problemas de Programação Linear de maximização.

## Tecnologias

- Java 21
- Apache Maven
- JUnit 5

## Funcionalidades

- Leitura de problemas a partir de arquivos `.txt`
- Conversão para a forma padrão
- Execução das Fases I e II do Simplex
- Remoção de variáveis artificiais
- Regra de Bland para seleção de pivôs
- Exibição dos tableaux durante as iterações
- Identificação de soluções ótimas, inviáveis e ilimitadas
- Contagem de pivoteamentos
- Validação de arquivos de entrada

## Requisitos

- JDK 21 ou superior compatível com o projeto
- Apache Maven instalado

## Compilação

Na raiz do projeto:

```bash
mvn clean package
```

## Execução

### Interface gráfica (recomendado)

Sem argumentos, o programa abre uma janela didática com o problema em notação matemática, o passo a passo das fases e o resultado interpretado:

```bash
java -cp target/classes br.ufc.simplex.Main
```

Na interface você pode:

- escolher um exemplo da pasta `entradas/`
- abrir outro arquivo `.txt`
- acompanhar Fase I (viabilidade), transição e Fase II (otimização)
- ler o status final com interpretação (ótima, inviável ou ilimitada)

### Terminal

```bash
java -cp target/classes br.ufc.simplex.Main entradas/exemplo01.txt
```

Substitua `exemplo01.txt` pelo arquivo desejado.

## Testes automatizados

```bash
mvn clean test
```

## Formato do arquivo de entrada

O arquivo deve conter, nesta ordem:

1. Quantidade de variáveis.
2. Coeficientes da função objetivo.
3. Quantidade de restrições do tipo `<=`, seguida de suas linhas.
4. Quantidade de restrições do tipo `=`, seguida de suas linhas.
5. Quantidade de restrições do tipo `>=`, seguida de suas linhas.

Cada restrição contém os coeficientes das variáveis e, por último, o termo independente.

Exemplo:

```text
2
4 3
1
1 3 20
1
1 -1 0
1
2 -1 1
```

O programa considera problemas de maximização com variáveis não negativas.

## Exemplos disponíveis

| Arquivo | Cenário |
|---|---|
| exemplo01.txt | Restrições mistas e solução ótima |
| exemplo02.txt | Problema inviável |
| exemplo03.txt | Problema ilimitado |
| exemplo04.txt | Restrições de menor ou igual |
| exemplo05.txt | Restrição redundante |
| exemplo06.txt | Pivoteamento de limpeza |
| exemplo07.txt | Solução básica degenerada |

## Estrutura do projeto

```text
simplex-java/
├── entradas/
├── src/
│   ├── main/java/br/ufc/simplex/
│   │   ├── Main.java
│   │   ├── io/
│   │   ├── model/
│   │   ├── output/
│   │   ├── solver/
│   │   └── ui/
│   └── test/java/br/ufc/simplex/
├── pom.xml
└── README.md
```
