# Como manter o CNPJ como inteiro no banco de dados em 2026

**Obs.**: é meme sa disgraça. Usem CHAR(14) no banco de dados. Faz favor.

Conforme demonstrado nessa [matéria](https://www.contabeis.com.br/noticias/65594/novo-cnpj-receita-federal-anuncia-mudancas-no-cadastro-de-empresas/), à partir de 2026, o valor do CNPJ passará à possuir caracteres alfanuméricos, quebrando compatibilidade com os conterrâneos que armazenam o CNPJ como um número... 

![Ou será que não?](https://media.tenor.com/n53AcEumMqQAAAAM/padrinhos-m%C3%A1gicos.gif)

Utilizando a [tabela ASCII](https://www.ascii-code.com/) e sua posição, é possível efetuar alguns cálculos para garantir a preservação dessa informação, tanto no momento da conversão para inteiro quanto na hora de mostrar a informação formatada como texto. O _drawback_ dessa abordagem é a necessidade de armazenar mais um campo como JSONB no banco de dados, para armazenar os caracteres e suas posições atuais, além de uma conversão mais complexa no geral.
