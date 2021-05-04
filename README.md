# To Printful Team

I know you guys would prefer Coroutines and Flow better but since this is a test task and 
I already wrote in a cover letter that I haven't worked with those technologies yet so I decided to stick 
to what I know best to really show what I am capable of doing in the limited time given. 

I started to migrate Rx-Java stuff to Coroutines. 
At first it was super easy. Single became suspend which was launched in a custom CoroutineScope and then cancelled in clear method. 
But when I started to refactor state, it became messy for me. 
I stumbled upon MutableStateFlow so the emitting of the state was quite straight forward. 
Error handling for coroutines was unfamiliar at best. 
When not wrapped in try catch it just silently forced closed activity for me but maybe my setup was incorrect. 
At that point I started to realize that I would not make it in time and another reason was that since I am new to this technology 
the code quality would suffer from this since I don't have good examples from which I could learn. 
