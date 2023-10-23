package xyz.fmcy.server.standard;

import java.util.Objects;
import java.util.function.Supplier;

public final class Results {
    public static <T> ResultIf<T> If(boolean condition, Supplier<Result<T>> resultSupplier) {
        return ResultIf.If(condition, resultSupplier);
    }

    public final static class ResultIf<T> {
        private final Supplier<Boolean> condition;
        private final Supplier<Result<T>> resultSupplier;
        private ResultIf<T> lastIf;
        private ResultIf<T> nextIf;

        private ResultIf(Supplier<Boolean> condition, Supplier<Result<T>> resultSupplier) {
            this.condition = Objects.requireNonNull(condition);
            this.resultSupplier = Objects.requireNonNull(resultSupplier);
            lastIf = this;
        }

        public static <T> ResultIf<T> If(Supplier<Boolean> condition, Supplier<Result<T>> resultSupplier) {
            return new ResultIf<>(condition, resultSupplier);
        }

        public static <T> ResultIf<T> If(boolean condition, Supplier<Result<T>> resultSupplier) {
            return new ResultIf<>(() -> condition, resultSupplier);
        }

        public ResultIf<T> elseIf(boolean condition, Supplier<Result<T>> resultSupplier) {
            return elseIf(() -> condition, resultSupplier);
        }

        public ResultIf<T> elseIf(Supplier<Boolean> condition, Supplier<Result<T>> resultSupplier) {
            ResultIf<T> anIf = If(condition, resultSupplier);
            lastIf.nextIf = anIf;
            lastIf = anIf;
            return this;
        }

        public Result<T> Else(Supplier<Result<T>> resultSupplier) {
            ResultIf<T> resultIf = this;
            while (!resultIf.condition.get()) {
                if ((resultIf = resultIf.nextIf) == null) {
                    return resultSupplier.get();
                }
            }
            return resultIf.resultSupplier.get();
        }
    }

}

