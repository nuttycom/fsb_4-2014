import java.util.*;

class EitherJava {
  interface EitherVisitor<A, B, C> {
    public C visitLeft(Left<A, B> left);
    public C visitRight(Right<A, B> right);
  }

  interface Either<A, B> {
    public <C> C accept(EitherVisitor<A, B, C> visitor);
  }

  public static final class Left<A, B> implements Either<A, B> {
    public final A value;
    public Left(A value) {
      this.value = value;
    }

    public <C> C accept(EitherVisitor<A, B, C> visitor) {
      return visitor.visitLeft(this);
    }
  }

  public static final class Right<A, B> implements Either<A, B> {
    public final B value;
    public Right(B value) {
      this.value = value;
    }

    public <C> C accept(EitherVisitor<A, B, C> visitor) {
      return visitor.visitRight(this);
    }
  }

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
}
