/* Initial beliefs and rules */
semaphore(free).
limit(goods, 5).

need_charge(Ag) :-
	.date(YY, MM, DD) &
	.count(consumed(YY, MM, DD, _, _, _, B), QtdB) & limit(B, Limit) & QtdB > Limit.

/* Plans library */

+!sendMsg(ask)
	: true
	<- .send(delivery, achieve, ask(delivery, goods)).

+!has(delivery, goods, Agent)
	: semaphore(free) & not need_charge(Agent) & not broken(_) & not rescuing(_)
    <- .print("Goal recived"); -semaphore(free); !at(Agent, delivery);
       unload(goods);
       utils.rand_int(R, 1, 4);
	   !at(Agent, R);
	   access(rack); place(R); free(rack); +semaphore(free); !sendMsg(ask);
	   .date(YY, MM, DD);
       .time(HH, NN, SS);
       +consumed(YY, MM, DD, HH, NN, SS, Agent).

/* se c'è l'ultimo pezzo da scaricare da la priorità rispetto alla carica */
+!has(delivery, lastPiece, Ag)
    : semaphore(free) & not broken(_) & not rescuing(_)
    <- .print("Received goal last piece"); -semaphore(free); !at(Agent, delivery);
       unload(goods);
       utils.rand_int(R, 1, 4);
       !at(Agent, R);
       access(rack); place(R); free(rack); +semaphore(free); !sendMsg(ask).

/*il robot ha bisogno di caricarsi */
+!has(delivery, goods, Agent)
    : semaphore(free) & need_charge(Agent) & not broken(_) & not rescuing(_)
    <- .print("I need to recharge");
       -consumed(_, _, _, _, _, _, Agent);
       -consumed(_, _, _, _, _, _, Agent);
       -consumed(_, _, _, _, _, _, Agent);
       -consumed(_, _, _, _, _, _, Agent);
       -consumed(_, _, _, _, _, _, Agent);
       !go(await, Agent);
       -semaphore(free);
       charge(0);
       .wait(800);
       charge(20);
       .wait(800);
       charge(40);
       .wait(800);
       charge(60);
       .wait(800);
       charge(80);
       .wait(800);
       charge(100);
       .wait(400);
       charge(-1);
       +semaphore(free);
       !sendMsg(ask).

/* l'agente ha ricevuto un ordine ma non è libero */
+!has(delivery, goods, Agent)
    : not semaphore(free)
    <- true.

+!go(await, Agent)
    : semaphore(free) & not at(waitingZone) & not broken(_) & not rescuing(_)
    <- .print("I'm going to wait"); -rackType(0); -semaphore(free); !at(Agent, waitingZone).

+!go(await, Agent)
    : semaphore(free) & at(waitingZone)
    <- .print("I'm already in waiting zone"); true.

+!go(await, Agent)
    : not semaphore(free) & at(waitingZone)
    <- .print("I'm already in waiting zone with busy semaphore"); true.

+!at(Agent, waitingZone)
    : not semaphore(free) & at(Agent, waitingZone)
    <- .print("I have reached my waiting zone"); +semaphore(free);
       .send(delivery, achieve, askOne(delivery, goods)).

+!at(Agent, P)
	: at(Agent, P)
	<- .wait(200).

+!at(Agent, P)
	: not at(Agent, P)
	<- move_towards(P); !at(Agent, P).





/* Handling breakdown */
/* breakdown first case */
+!has(delivery, goods, Agent)
    : semaphore(free) & broken(Rescuer)
    <- -semaphore(free); .print("BREAKDOWN"); show_broken(Agent); .send(Rescuer, achieve, provide_help(Agent)); !fault_resolved(Rescuer).

/* breakdown second case */
+!go(await, Agent)
    : semaphore(free) & broken(Rescuer)
    <- -rackType(0); -semaphore(free); .print("BREAKDOWN") show_broken(Agent); .send(Rescuer, achieve, provide_help(Agent)); !fault_resolved(Rescuer).

/* breakdown third case: last piece */
+!has(delivery, lastPiece, Agent)
    : semaphore(free) & broken(Rescuer)
    <- -semaphore(free); !unmanaged(last_piece); .print("BREAKDOWN"); show_broken(Agent); .send(Rescuer, achieve, provide_help(Agent)); !fault_resolved(Rescuer).

/* breakdown fourth case: low battery */
+!has(delivery, goods, Agent)
    : semaphore(free) & need_charge(Agent) & broken(Rescuer)
    <- -semaphore(free); .print("BREAKDOWN"); show_broken(Agent); .send(Rescuer, achieve, provide_help(Agent)); !fault_resolved(Rescuer).

/* random movement */
+!fault_resolved(Rescuer)
	: not fault_resolved(Rescuer)
	<- move_towards(broken); .wait(500); !fault_resolved(Rescuer).

/* Random movement until rescued */
+!fault_resolved(Rescuer)
	: fault_resolved(Rescuer)
	<- .print("I have been reached for rescue");
	   .wait(4500);
	   restore(env);
       +semaphore(free);
       !sendMsg(ask).

+!provide_help(Agent)
    : true
    <- +rescuing(Agent).


/* Rescue management */
/* rescue request first case */
+!has(delivery, goods, Ag)
    : semaphore(free) & rescuing(Agent)
    <- -semaphore(free); .print("I go to help"); !reach_robot(Agent).

/* rescue request second case */
+!go(await, Ag)
    : semaphore(free) & rescuing(Agent)
    <- -rackType(0); -semaphore(free); .print("I go to help"); !reach_robot(Agent).

/* rescue request third case */
+!has(delivery, lastPiece, Ag)
    : semaphore(free) & rescuing(Agent)
    <- -semaphore(free); !unmanaged(last_piece); .print("I go to help"); !reach_robot(Agent).

/* rescue request fourth case */
+!has(delivery, goods, Ag)
    : semaphore(free) & need_charge(_) & rescuing(Agent)
    <- -semaphore(free); .print("I go to help"); !reach_robot(Agent).

+!reach_robot(Agent)
	: not reach_robot(Agent)
	<- catch(Agent); move_towards(Agent); !reach_robot(Agent).

+!reach_robot(Agent)
	: reach_robot(Agent)
	<- .print("Broken agent reached");
	   show_recovery(Agent);
	   .wait(4500);
	   -rescuing(Agent);
	   restore(env);
	   +semaphore(free);
       !sendMsg(ask).

+!unmanaged(last_piece)
    : true
    <- .send(delivery, achieve, unmanaged(last_piece)).

/* Plans failure handling*/
-!has(_, _, _)
	: true
	<- .current_intention(I);
		.print("Failed to achieve goal '!has(_, _, _)'. Current intention is: ", I). // print debug info



