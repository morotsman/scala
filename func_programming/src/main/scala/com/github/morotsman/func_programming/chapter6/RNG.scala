package com.github.morotsman.func_programming.chapter6

trait RNG {
  def nextInt: (Int, RNG)
  

    
}

case class SimpleRNG(seed: Long) extends RNG {
    def nextInt: (Int, RNG) = {
      val newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL 
      val nextRNG = SimpleRNG(newSeed) 
      val n = (newSeed >>> 16).toInt 
      (n, nextRNG) 
    }
    

}

object RNG {
  def nonNegativeInt(rng: RNG): (Int, RNG) = {
    val (res, rng2) = rng.nextInt
    val positiveRes = if (res < 0) Math.abs(res + 1) else res
    (positiveRes, rng2)
  } 
  
  def double(rng: RNG): (Double, RNG) = {
    val (res, rng2) = nonNegativeInt(rng)
    (res / (Int.MaxValue.toDouble + 1), rng2)
  }
  
  def intDouble(rng: RNG): ((Int, Double), RNG) = {
    val (res, rng2) = rng.nextInt
    val (res2, rng3) = rng2.nextInt
    ((res, res2.toDouble), rng3)
  }
  
  def doubleInt(rng: RNG): ((Double, Int), RNG) = {
    val (res, rng2) = rng.nextInt
    val (res2, rng3) = rng2.nextInt
    ((res.toDouble, res2), rng3)    
  }
  
  def double3(rng: RNG): ((Double, Double, Double), RNG) = {
    val (res, rng2) = rng.nextInt
    val (res2, rng3) = rng2.nextInt   
    val (res3, rng4) = rng3.nextInt 
    ((res.toDouble, res2.toDouble, res3.toDouble), rng4)
  }
  
  def ints(count: Int)(rng: RNG): (List[Int], RNG) = {
    if(count == 0) (Nil, rng)
    else {
      val (res, rng2) = rng.nextInt
      val (list, rng3) = ints(count-1)(rng2)
      (res::list, rng3)
    }
  }
  
  type Rand[+A] = RNG => (A, RNG)
  
  val int: Rand[Int] = _.nextInt
  
  def unit[A](a: A): Rand[A] = 
    rng => (a, rng)
    
  def map[A, B](s: Rand[A])(f: A => B): Rand[B] = 
    rng => {
      val (a, rng2) = s(rng)
      (f(a), rng2)
    }
    
  def nonNegativeEven: Rand[Int] = 
    map(nonNegativeInt)(i => i - i % 2)
    
  def doubleInTermsOfMap: Rand[Double] = 
    map(nonNegativeInt)(res => res / (Int.MaxValue.toDouble + 1))

  def map2[A, B, C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] = 
    rng => {
      val (a, rng2) = ra(rng)
      val (b, rng3) = rb(rng2)
      (f(a,b), rng3)
    }
    
  def both[A, B](ra: Rand[A], rb: Rand[B]): Rand[(A, B)] = 
    map2(ra, rb)((a, b) => (a,b))
    
  val randIntDouble: Rand[(Int, Double)] = both(int, double)
  
  val randDoubleInt: Rand[(Double, Int)] = both(double, int)
  
  def sequence[A](fs: List[Rand[A]]): Rand[List[A]] = 
    fs.foldRight((rng: RNG) => (Nil: List[A], rng))((cur, acc) => map2(cur, acc)((v,a) => v::a))
    
  def intsInTermsOfSequence(count: Int): Rand[List[Int]] = 
    sequence(List.fill(count)(rng => rng.nextInt))
    
  def flatMap[A, B](f: Rand[A])(g: A => Rand[B]): Rand[B] = 
    rng => {
      val (res, rng2) = f(rng)
      g(res)(rng2)
    }
    
  def mapInTermsOfFlatMap[A, B](s: Rand[A])(f: A => B): Rand[B] =
    flatMap(s)(a => rng => (f(a), rng))
    
  def map2InTermsOfFlatMap[A, B, C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] = 
    flatMap(ra)(a => map(rb)(b => f(a, b))) 
}

case class State[S, +A](run: S => (A, S)) {
  
}

object State{
  
}