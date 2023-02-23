# TCC *vs* LCC

Explain under which circumstances *Tight Class Cohesion* (TCC) and *Loose Class Cohesion* (LCC) metrics produce the same value for a given Java class. Build an example of such as class and include the code below or find one example in an open-source project from Github and include the link to the class below. Could LCC be lower than TCC for any given class? Explain.

## Answer

# TCC vs LCC

In this answer, we consider only public methods.

First, we make a graph from the public methods available in the class we are studying. The nodes will be the methods of the class, and the edges will represent the fact that the 2 related methods are using one attribute of the class in common.

TCC = Amount of path of length 1, divided by the total amount of (method-method) pairs possible.
LCC = Amount of path of length n (n being a whole and strictly positive number), divided by the total amount of (method-method) pairs possible.

```
class C {
	private Type1 p1;

	public void stuff1() {
		return p1;
	}


	public void stuff2() {
		return "Hello world!";
	}
}
```
In the above example we have a total of 1 possible (method-method) pair, (stuff1-stuff2).
There are no edges between stuff1 and stuff2.
Thus TCC = 0/1 = 0.

Since there are not a single edge, there are neither a path of length n > 0.
Thus, LCC = 0/1 = 0.

Therefore TCC = LCC = 0

```
class C {
	private Type1 p1;

	public Type1 getP1() {
		return p1;
	}


	public void setP1(p1) {
		this.p1 = p1;
	}
}
```

Here we have only one edge, thus one path of length 1.
Therefore, TCC = LCC = 1.


# Could LCC be lower than TCC for any given class?
No. LCC comprises all the path of length 1 in addition to the longer ones.
TCC is made up of only the path of length 1. In this respect, LCC is greater or equal to TCC.