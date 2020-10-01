package biz.dealnote.messenger.domain.mappers;

public interface MapF<O, R> {
    R map(O orig);
}