/* Initial beliefs */

/* Plans library */

+!order(Product,Qtd)[source(Ag)]
	: true
	<- deliver(Product,Qtd); .send(delivery, achieve, done(delivery)).

