package scalaz

/** A cofree comonad for some functor `S`, i.e. an `S`-branching stream. */
case class Cofree[S[+_], +A](head: A, tail: S[Cofree[S, A]])(implicit S: Functor[S]) {
  final def map[B](f: A => B): Cofree[S, B] =
    apply(f, _ map f)

  /** Alias for `extend` */
  final def =>>[B](f: Cofree[S, A] => B): Cofree[S, B] = this extend f

  /** Redecorates this structure with a computation whose context is the entire structure under that value. */
  final def extend[B](f: Cofree[S, A] => B): Cofree[S, B] =
    applyTail(f(this), _ extend f)

  /** Redecorates the structure with values representing entire substructures. */
  final def duplicate: Cofree[S, Cofree[S, A]] = 
    applyTail(this, _.duplicate)

  /** Returns the components of this structure in a tuple. */
  final def toPair: (A, S[Cofree[S, A]]) = (head, tail)

  /** Changes the branching functor by the given natural transformation. */
  final def mapBranching[T[+_]:Functor](f: S ~> T): Cofree[T, A] =
    Cofree(head, f(S.map(tail)(_ mapBranching f)))

  /** Modifies the first branching with the given natural transformation. */
  final def mapFirstBranching(f: S ~> S): Cofree[S, A] =
    Cofree(head, f(tail))

  /** Injects a constant value into this structure. */
  final def inject[B](b: B): Cofree[S, B] =
    applyTail(b, _ inject b)

  /** Applies `f` to the head and `g` through the tail. */
  final def apply[B](f: A => B, g: Cofree[S, A] => Cofree[S, B]): Cofree[S, B] =
    Cofree(f(head), S.map(tail)(g))

  /** Replaces the head with `b` and applies `g` through the tail. */
  final def applyTail[B](b: B, g: Cofree[S, A] => Cofree[S, B]): Cofree[S, B] =
    apply(x => b, g)

  /** Applies a function `f` to a value in this comonad and a corresponding value in the dual monad, annihilating both. */
  final def zap[G[+_], B, C](bs: Free[G, B])(f: (A, B) => C)(implicit G: Functor[G], d: Duality[S, G]): C =
    Duality.comonadMonadDuality.zap(this, bs)(f)

  /** Applies a function in a monad to the corresponding value in this comonad, annihilating both. */
  final def smash[G[+_], B](fs: Free[G, A => B])(implicit G: Functor[G], d: Duality[S, G]): B =
    zap(fs)((a, f) => f(a))
}

