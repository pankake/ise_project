/* Initial beliefs */

available(goods).
counter(goods, 0).

/* Initial goals */
!sendMsg(goods).

/* Plans Library */

+!sendMsg(goods)
	: true
	<- .print("Sending goals!"); .send(robot, achieve, has(delivery, goods, robot));
	 .send(robot1, achieve, has(delivery, goods, robot1)).

+!sendMsgAwait(Ag)
	: true
	<- .send(Ag, achieve, go(await, Ag)).

+!ask(delivery, goods)
	: available(goods) & not stock(goods, 1)
	<- !sendMsg(goods).

+!ask(delivery, goods)[source(Ag)]
	: available(goods) & stock(goods, 1)
	<- -available(goods); .send(Ag, achieve, has(delivery, lastPiece, Ag)).

+!ask(delivery, goods)[source(Ag)]
	: not available(goods)
	<- .print("Msg received before the stock update, i pause:", Ag);
	!sendMsgAwait(Ag).

+!askOne(delivery, goods)
	: available(goods) & not stock(goods, 1)
	<- !sendMsg(goods).

+!askOne(delivery, goods)
	: not available(goods)
	<- true.

+stock(goods, 0)
	: available(goods)
	<- -available(goods).

+stock(goods, N)
	: N > 1 & not available(goods)
	<- -+available(goods); !sendMsg(goods).

@waitfor
+end(goods)
	: not delivering
	<- !orderGoods.

+!orderGoods
    : counter(goods, Value) & Value < 100
    <- .print("Finished goods i place a new order");
       +delivering; utils.rand_int(N, 3, 9);
       .send(truck, achieve, order(goods, N));
       -counter(goods, Value); +counter(goods, N + Value).

-!orderGoods
    : counter(goods, Value) & Value >= 100
    <- .print("Cargo limit reached:", Value).

+!done(delivery)
    : true
    <- -delivering.

+!unmanaged(last_piece)
    : true
    <- .print("Last piece unmanaged signal received"); +available(goods).



