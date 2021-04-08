package org.mbari;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * @author Brian Schlining
 * @since 2017-05-10T09:55:00
 */
public class EventBus {

    private final Subject<Object> rxSubject = PublishSubject.create().toSerialized();

    public void send(Object o) {
        if (o != null) {
            rxSubject.onNext(o);
        }
    }

    public Observable<Object> toObserverable() {
        return rxSubject;
    }
}