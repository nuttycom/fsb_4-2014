# How To Write A Perfect Program

[Kris Nuttycombe](http://github.com/nuttycom) -- [`@nuttycom`](http://twitter.com/nuttycom)

May 13, 2014

---------

<img src="./img/ras_anvil.jpg" width="400"/>

> "Let me take you on an adventure which will give you superpowers." --[`@bitemyapp`](https://twitter.com/bitemyapp/status/455464035987623936)

<https://github.com/nuttycom/startup_week-2014>

<div class="notes">

The First Form

Not on my board / trail of awesome.

Haskell and Scala

* Mention utility of types. - Piper
* Going to start with a very simple program.

</div>

-------

# Start Very Simple

~~~{.haskell }

True
  
~~~

This program doesn't have any bugs.

<div class="notes">

This is a very simple program - a value interpreted by an interpreter.

How the interpreter presents that value to the person running it can vary.

Represents a single state.

</div>

<div class="fragment">

~~~{.haskell }

nuttycom@crash: ~ $ ghci
GHCi, version 7.6.3: http://www.haskell.org/ghc/  :? for help
Loading package ghc-prim ... linking ... done.
Loading package integer-gmp ... linking ... done.
Loading package base ... linking ... done.
Prelude> True
True
  
~~~


</div>

--------

## Learn To Count

~~~{.haskell}

data Bool = True | False

boolProgram :: Bool
-- "::" is pronounced, "has type"
  
~~~

How many states could boolProgram inhabit?

<div class="fragment">

1 + 1 = 2 

> applause

</div>

<div class="fragment">

~~~{.haskell}

intProgram :: Int32
  
~~~

</div>

<div class="fragment">

This one is just awful...

~~~{.haskell}

strProgram :: String
  
~~~

</div>

--------

## Learn to Count

~~~{.haskell}

-- if haskell had unsafe type-level pattern matching... 
inhabitants :: Type -> Nat

inhabitants Bool = 2
inhabitants Int32 = 2^32
  
~~~

--------

## Learn to Add

~~~{.haskell}

-- Maybe there's one of these...
data Maybe a = Just a | Nothing

inhabitants (Maybe a) = inhabitants a + 1

-- Either there's one of these, or one of those...
data Either a b = Left a | Right b

inhabitants (Either a b) = inhabitants a + inhabitants b
  
~~~

Bool, Maybe and Either are called sum types for the obvious reason.

--------

## Learn to Multiply

~~~{.haskell}

tuple :: (Bool, Int32)
  
~~~

* `fst :: (a, b) -> a`
    - `(fst tuple)` has 2 inhabitants
<br/><br/>
* `snd :: (a, b) -> b`
    - 2^32^ inhabitants if `(fst tuple) == True`
    - 2^32^ inhabitants if `(fst tuple) == False`
<br/><br/>
* 2 * 2^32^ = 2^33^

With tuples, we always multiply.

--------

## Learn to Multiply

~~~{.haskell}

inhabitants (a, b) = inhabitants a * inhabitants b
inhabitants (Int32, Int32) = 2^32 * 2^32 = 2^64

inhabitants (a, b, c) = inhabitants a * inhabitants b * inhabitants c
inhabitants (Bool, Bool, Int32) = 2 * 2 * 2^32 = 2^34
  
~~~

We call these "product" types.

--------

## Isomorphic Types

These types have the same inhabitants.

~~~{.haskell}

-- expressed as a product
tuple :: (Bool, Int32) -- 2^33 inhabitants

-- expressed as a sum
either :: Either Int32 Int32 -- 2^33 inhabitants
  
~~~

Choose whichever one is most convenient

<div class="fragment">

~~~{.haskell}

eitherBoolOrInt :: Either Bool Int32 -- 2 + 2^32 inhabitants
  
~~~

Either is more flexible

</div>

---------

## Add if you can, multiply if you must

~~~{.haskell}

inhabitants (Either Bool Int32) = 2 + 2^32
  
~~~

Most languages emphasize products.
<br/><br/>

Bad ones don't let you define a type <br/>with 2 + 2^32^ inhabitants easily.

<div class="notes">

Sapir-Whorf

Most mainstream languages today make products easy, sums hard.

Too easy to define types with too many inhabitants.

</div>

---------

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
  public Left(A value) {
    this.value = value;
  }

  public <C> C accept(EitherVisitor<A, B, C> visitor) {
    return visitor.visitLeft(this);
  }
}

public final class Right<A, B> implements Either<A, B> {
  public final B value;
  public Right(B value) {
    this.value = value;
  }

  public <C> C accept(EitherVisitor<A, B, C> visitor) {
    return visitor.visitRight(this);
  }
}
~~~

--------

> "Bug fixing strategy: forbid yourself to fix the bug. Instead, render 
> the bug impossible by construction." 
> --[Paul Phillips](https://twitter.com/extempore2/status/417366903209091073)

--------

# The Vampire Policy

![](./img/bela-lugosi.jpg)

> Don't let him in.

<div class="notes">

To do this, we need to minimize the state space of our program.

What types should we not let in?

</div>

--------

# Strings (Ugh.)

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

# Stake

## import scalaz._

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
