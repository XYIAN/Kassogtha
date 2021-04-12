package org.mbari;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * README: I've added this in case we needs an event bus later. EventBuses are a great way to
 * allow UI components to talk to each other. Without an eventbus each componcnet would need a
 * reference to every othe component it interacts with. With an event bus, a componenet only needs
 * a reference to the eventbus, it publishes messages and other componeents can listen for and act
 * on those messages if needed
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
