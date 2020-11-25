# Delayed messaging with RabbitMQ


[![pipeline status](https://gitlab.com/alex.vaitsekhovich/delayed-messaging-with-rabbitmq/badges/main/pipeline.svg)](https://gitlab.com/alex.vaitsekhovich/delayed-messaging-with-rabbitmq/pipelines)

[![codecov](https://codecov.io/gh/alexvaitsekhovich/delayed-messaging-with-rabbitmq/branch/main/graph/badge.svg?token=0GYptlOxgB)](https://codecov.io/gh/alexvaitsekhovich/delayed-messaging-with-rabbitmq) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/ca1d6c9dc7e44403b2c73834a0ea0d55)](https://www.codacy.com/gh/alexvaitsekhovich/delayed-messaging-with-rabbitmq/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=alexvaitsekhovich/delayed-messaging-with-rabbitmq&amp;utm_campaign=Badge_Grade)
[![Maintainability](https://api.codeclimate.com/v1/badges/c6d5ea81364980f33357/maintainability)](https://codeclimate.com/github/alexvaitsekhovich/delayed-messaging-with-rabbitmq/maintainability)

## Concept ##

Sometimes we need to perform actions with a certain delay. For example, in an ordering system we want to remind customers to leave a review of the order. This can be performed with a traditional solution - batch script for checking the database, or programmatical solution - with Java DelayQueue. 

In this system the messaging delay is implemented using RabbitMQ dead-letter concept: a queue can have a time-to-live span, after the TTL has expired, the message will be dropped, but if dead-letter-exchange was defined for the queue, the message will be redirected to that exchange. 

Representing the delay as a binary digit, we can build a chain of queues and dead-letter exchanges attached to them, where '1' in this digit will make the message remain in the corresponding queue until the TTL will expire and '0' will make the message fall through without delay.

## System ##

First, the system is initialized with multiple levels of delaying queues with reasonable value for the maximal delay. This system consists of four parts:

#### Receiving module:
Message is published to the check-in exchange. The attached queue receives the message and checks it. If the defined delay is greater than the maximal delay, the message delay will be reduced, substracting the maximal delay, and the message will be published to the meditating part. If the defined delay is less than the maximal delay, a topic for the message will be calculated, representing a delay time in binary form and the original message routing key attached at the end.

#### Meditating module:
The queue here has a fixed TTL of maximal delay. After TTL expires, messages will be redirected to the receiving part again.

#### Delaying module:
This part consists of a chain of exchanges. Each exchange has two queues attached to them - one delay queue with TTL corresponding to the level (1 second for the lowest level, 2 seconds for one level up, etc.) and pass-through queue with TTL of 1 ms. 

#### Distriburting module:
The last exchange in the delaying module has a distributing queue attached to it. The distributing queue consumes the message, removes the topic with the delaying information and publishes the original message with original routing key to the pick-up exchange, form where the message can be consumed by their final consumer queue. 

<p align="center">
<img src="https://github.com/alexvaitsekhovich/images/blob/main/mqdelay.png" width="802" height="629" alt="MQ-delay">
  
  
## Example ##

In this example the system was initialized with four levels of delay: queues with TTL of 1, 2, 4, and 8 seconds.

Now a messages with a expected delay of 27 seconds and routing key _"my-key"_ is published into the check-in exchange. Now the process starts:

* The delay is higher than our maximal delay of _8 seconds_, so it should be redirected to the meditating queue that has 8 seconds TTL. Why not to the first exchange of the delaying module? To keep the delaying module only for messages in delay process, not for the waiting ones. 
* The preparing queue subtracts the maximal delay from the expected delay (_19 seconds_ remain) and sends it to the meditating queue.
* TTL in the meditating queue expires and the message is redirected to the check-in exchange.
* The delay is still to big, so the message is resent to the meditating queue with defined delay of _11 seconds_.
* TTL in the meditating queue exprires. The remaining delay is now _11 seconds_ The preparing queue converts it into binary representation and attaches the original routing key: so, _11 seconds_ and the routing key _"my-key"_ will be converted to the topic `1.0.1.1.me-key`.
* Message is published to the _exchange_level_1000_ where it will be consumed by the _delay_queue_1000_, that corresponds to the topic `1.*.*.*.*`.
* After the TTL of 8 seconds expires, the message is redirected to the _exchange_level_0100_, will be consumed by the _pass_queue_0100_, because the topic `*.0.*.*.*` matches.
* After only 1 ms of delay the message is redirected to the _exchange_level_0010_ and consumed by the _delay_queue_0010_.
* After the TTL of 2 seconds expires, the message is redirected to the _exchange_level_0001_ and consumed by the _delay_queue_0001_.
* After the TTL of 1 second expires, the message is redirected to the _exchange_level_0000_ and consumed by the _destributing queue_.
* The _distributing queue_ removes the topic with delay information, sets the routing key back to _"my-key"_ and publishes it to the _pick-up exchange_ from where the message can be consumed by the final receiver.

