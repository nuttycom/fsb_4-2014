# How To Write A Perfect Program

[Kris Nuttycombe](http://github.com/nuttycom) -- [`@nuttycom`](http://twitter.com/nuttycom)

Dec 10, 2014

---------

<img src="./img/ras_anvil.jpg" width="400"/>

> "Let me take you on an adventure which will give you superpowers." --[`@bitemyapp`](https://twitter.com/bitemyapp/status/455464035987623936)

<https://github.com/nuttycom/fsb_4-2014>

<div class="notes">

The First Form

Not on my board / trail of awesome.

Utility of types - we want machines to help us as much as they can. 

At the time you write the code, you have a mental model of what operations are
legal with a given value. That mental model is a type.

</div>

-------

## The Simplest Program Possible

This program doesn't have any bugs:

~~~{.haskell }

()
  
~~~

<div class="notes">

This is a very simple program - a value interpreted by an interpreter.

How the interpreter presents that value to the person running it can vary.

Only has one state.

The empty tuple - does everybody know the difference between a tuple and a list?

</div>

<div class="fragment">

~~~{.haskell }

kris@floorshow ~ » ghci
GHCi, version 7.8.3: http://www.haskell.org/ghc/  :? for help
Loading package ghc-prim ... linking ... done.
Loading package integer-gmp ... linking ... done.
Loading package base ... linking ... done.

Prelude> ()
()
  
~~~

</div>

--------

## Learn To Count

~~~{.haskell}

data Bool = True | False
  
~~~

<div class="fragment">
~~~{.haskell}

boolProgram :: Bool
boolProgram = --hidden
  
~~~

<div class="notes">

"::" is pronounced "has type"

</div>

How many states can a program of type Bool be inhabited by?

</div>

<div class="fragment">

~~~{.haskell}

-- let's imagine haskell had type-level functions ... 
inhabitants :: Type -> Nat

inhabitants Bool = inhabitants True + inhabitants False
inhabitants Bool = 1 + 1
inhabitants Bool = 2
  
~~~

</div>

--------

## Counting Higher

How many states in a program does a value of type Int32 represent?

~~~{.haskell}

intProgram :: Int32
  
~~~

<div class="fragment">

~~~{.haskell}

inhabitants Int32 = inhabitants -2147438648 
                  + inhabitants -2147439647
                  + ...
                  + inhabitants 2147439647

inhabitants Int32 = 2^32
  
~~~
</div>

<div class="fragment">

This one is just awful...

~~~{.haskell}

strProgram :: String

inhabitants String = ?????
  
~~~

</div>

--------

## Learn to Add

~~~{.haskell}

data Bool = True | False

inhabitants Bool = 1 + 1

-- Maybe there's one of these...
data Maybe a = Just a | Nothing

inhabitants (Maybe a) = inhabitants a + 1

-- Either there's one of these, or one of those...
data Either a b = Left a | Right b

inhabitants (Either a b) = inhabitants a + inhabitants b
  
~~~

Bool, Maybe and Either are called "sum" types.

--------

## Learn to Multiply

~~~{.haskell}

boolAndInt :: (Bool, Int32)

inhabitants (Bool, Int32) = inhabitants Bool ??? inhabitants Int32

                          = inhabitants Int32 -- if _1 == True
                          + inhabitants Int32 -- if _1 == False
  
                          = inhabitants Bool * inhabitants Int32

                          = 2 * 2^32

                          = 2^33
~~~

Tuples (and objects) are called "product" types.

--------

## Different Shapes, Same Implications

~~~{.haskell}

inhabitants (Bool, Int32)      = inhabitants Bool * inhabitants Int32
                               = 2 * 2^32
                               = 2^33 

inhabitants Either Int32 Int32 = inhabitants Int32 + inhabitants Int32
                               = 2^32 + 2^32
                               = 2^33
  
~~~

These types are *isomorphic*

<div class="fragment">
~~~{.haskell}

toEither :: (Bool, a) -> Either a a
toEither (True, a) = Right a
toEither (False, a) = Right a

fromEither :: Either a a -> (Bool, a)
fromEither (Right a) = (True, a)
fromEither (Left a)  = (False, a)
  
~~~
</div>


---------

## Add if you can, multiply if you must

~~~{.haskell}

inhabitants Either Bool Int32 = inhabitants Bool + inhabitants Int32
                              = 2 + 2^32
  
~~~

Most languages emphasize products.

Bad ones don't let you define a type <br/>with 2 + 2^32^ inhabitants easily.

<div class="notes">

Sapir-Whorf

Most mainstream languages today make products easy, sums hard.

Too easy to define types with too many inhabitants.

</div>

---------

## Haskell

~~~{.haskell}

Prelude> data Either a b = Left a | Right b

Prelude> let printEither (Left b)  = putStrLn $ "It's a left: "  ++ show (not b)
Prelude|     printEither (Right i) = putStrLn $ "It's a right: " ++ show (i + 1)
Prelude| in  traverse printEither [Left True, Right 1, Left False, Right 2]

It's a left: False
It's a right: 2
It's a left: True
It's a right: 3
[(),(),(),()]
  
~~~

---------

## Scala

~~~{.scala}

scala> :paste
// Entering paste mode (ctrl-D to finish)

sealed trait Either[A, B]
case class Left[A, B](a: A) extends Either[A, B]
case class Right[A, B](b: B) extends Either[A, B]

// Exiting paste mode, now interpreting.

scala> val xs : List[Either[Boolean, Int]] =
     |   List(Left(true), Right(1), Left(false), Right(2))

scala> xs foreach {
     |   case Left(b)  => println(s"It's a left: ${!b}")
     |   case Right(i) => println(s"It's a right: ${i + 1}")
     | }
It's a left: false
It's a right: 2
It's a left: true
It's a right: 3
  
~~~

--------

## JavaScript
~~~{.javascript}
function left(a) {
  return function(ifLeft, ifRight) { return ifLeft(a); };
}

function right(b) {
  return function(ifLeft, ifRight) { return ifRight(b); };
}
~~~

~~~{.javascript}
var xs = [left(true), right(1), left(false), right(2)];

for (i = 0; i < xs.length; i += 1) {
  var eitherVal = xs[i]
  eitherVal(
    function(b) { console.log("It's a left: " + !b) },
    function(i) { console.log("It's a right: " + (i + 1)) }
  )
}
~~~

~~~{.javascript}
It's a left: false
It's a right: 2
It's a left: true
It's a right: 3
~~~

--------

## Java
~~~{.java}
interface EitherVisitor<A, B, C> {
  public C visitLeft(Left<A, B> left);
  public C visitRight(Right<A, B> right);
}

interface Either<A, B> {
  public <C> C accept(EitherVisitor<A, B, C> visitor);
}

public final class Left<A, B> implements Either<A, B> {
  public final A value;
  public Left(A value) { this.value = value; }

  public <C> C accept(EitherVisitor<A, B, C> visitor) {
    return visitor.visitLeft(this);
  }
}

public final class Right<A, B> implements Either<A, B> {
  public final B value;
  public Right(B value) { this.value = value; }

  public <C> C accept(EitherVisitor<A, B, C> visitor) {
    return visitor.visitRight(this);
  }
}
~~~

--------

## Java
~~~{.java}
  public static void main(String[] argv) {
    List<Either<Boolean, Integer>> xs = new ArrayList<>();
    xs.add(new Left<Boolean, Integer>(true));
    xs.add(new Right<Boolean, Integer>(1));
    xs.add(new Left<Boolean, Integer>(false));
    xs.add(new Right<Boolean, Integer>(2));

    for (Either<Boolean, Integer> x : xs) {
      x.accept(
        new EitherVisitor<Boolean, Integer, Void>() {
          public Void visitLeft(Left<Boolean, Integer> left) {
            System.out.println("It's a left: " + (!left.value));
            return null;
          }

          public Void visitRight(Right<Boolean, Integer> right) {
            System.out.println("It's a right: " + (right.value + 1));
            return null;
          }
        }
      );
    }
  }
~~~

--------

## Java
~~~{.java}

kris@heartland ~/personal/fsb_4-2014/src/main/java » java EitherJava
It's a left: false
It's a right: 2
It's a left: true
It's a right: 3
  
~~~

--------

> "Bug fixing strategy: forbid yourself to fix the bug. Instead, render 
> the bug impossible by construction." 
> --[Paul Phillips](https://twitter.com/extempore2/status/417366903209091073)

--------

# The Vampire Policy

![](./img/bela-lugosi.jpg)

> Don't invite errors into your program.

<div class="notes">

To do this, we need to minimize the state space of our program.

What types should we not let in?

</div>

--------

## Strings (Ugh.)

> The type `String` should only ever appear in your program when a value is being shown to a human being.

## Two common offenders

* Strings as dictionary keys
* Strings as serialized form

<div class="notes">

Strings are worse than any other potentially infinite data structure
because they're convenient.

A very common misfeature is to use strings as a serialization mechanism.

Appealing because human readable.

Introduce bugs so that you can have a debugging tool?

Good for being read by humans. But not for machines. Don't turn your value
into a string until it's about to be turned into photons headed at eyeballs.

</div>

--------

## Garlic

Use newtypes liberally.

~~~{.haskell}

newtype Name = Name { strValue :: String }
-- don't export strValue unless you really, really need it
  
~~~

~~~{.scala}

case class Name(strValue: String) extends AnyVal
  
~~~

**Never, ever pass bare String values<br/>unless it's to `putStrLn` or equivalent.**

**Never, ever return bare String values <br/>except from `readLn` or equivalent**

<div class="notes">

If a string is serving any purpose other than display in your system, then it
has semantic meaning that should be tracked by the type system. 

</div>

--------

## Holy Water

Hide your newtype constructor behind validation.

~~~{.scala}

case class IPAddr private (addr: String) extends AnyVal

object IPAddr {
  val pattern = """(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})""".r

  def parse(s: String): Option[IPAddr] = for {
    xs <- pattern.unapplySeq(s) if xs.forall(_.toInt <= 255)
  } yield IPAddr(s)
}
  
~~~

We've shrunk down an infinite number of states to (256^4^ + 1).
Given the inputs, that's the best we can do.

<div class="notes">

This approach applies to virtually every primitive type. 

</div>

--------

## Stake

**import scalaz._**

Three very useful types:

* `A \/ B`

* `Validation[A, B]`

* `EitherT[M[_], A, B]`

<div class="notes">

Sum types where the error type conventionally comes on the left.

* Going to quickly look at each of them.

</div>

--------

**`MyError \/ B`**

* `\/` (Disjunction) has a Monad biased to the right

* We can *sequentially* compose operations that might fail 

* `for` comprehension syntax is useful for this

~~~{.scala}

import scalaz.std.syntax.option._

def parseJson(s: String): ParseError \/ JValue = ???
def ipField(jv: JValue): ParseError \/ String = ???

def parseIP(addrStr: String): ParseError \/ IPAddr = 
  IPAddr.parse(addrStr) toRightDisjunction {
    ParseError(s"$addrStr is not a valid IP address")
  }

val ipV: ParseError \/ IPAddr = for {
  jv <- parseJson("""{"hostname": "nuttyland", "ipAddr": "127.0.0.1"}""")
  addrStr <- ipField(jv)
  ipAddr <- parseIP(addrStr)
} yield ipAddr
  
~~~

--------

**`Validation[NonEmptyList[MyError], B]`**

* Validation **does not** have a Monad instance.

* Composition uses Applicative: conceptually parallel!

* if you need sequencing, `.disjunction`

~~~{.scala}

type VPE[B] = Validation[NonEmptyList[ParseError], B]

def hostname(jv: JValue): VPE[String] = ???

def ipField(jv: JValue): ParseError \/ String = ???
def parseIP(addrStr: String): ParseError \/ IPAddr = ???

def host(jv: JValue) = ^[VPE, String, IPAddr, Host](
  hostname(jv), 
  (ipField(jv) >>= parseIP).validation.leftMap(nels(_)) 
) { Host.apply _ }
  
~~~

<div class="notes">

There is a functor isomorphism between `\/` and validation.

Types have the same information content, but compose differently.

</div>

--------

**`EitherT[M[_], MyErrorType, B]`**

* EitherT layers together two effects:

    - The "outer" monadic effect of the type constructor M[_]

    - The disjunctive effect of `\/`

~~~{.scala}

// EitherT[M, A, _] <~> M[A \/ _]

def findDocument(key: DocKey): EitherT[IO, DBErr, Document] = ???
def storeWordCount(key: DocKey, wordCount: Long): EitherT[IO, DBErr, Unit] = ???

val program: EitherT[IO, DBErr, Unit] = for {
  doc <- findDocument(myDocKey)
  words = wordCount(doc)
  _ <- storeWordCount(myDocKey, words)
} yield ()
  
~~~

<div class="notes">

"Real world" interaction.

</div>

--------

# Questions?
