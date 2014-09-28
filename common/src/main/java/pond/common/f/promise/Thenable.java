/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pond.common.f.promise;

import pond.common.f.Function;

/**
 * @author ed
 */
public interface Thenable<T> {

    public <NEW> Thenable<NEW> then(Function<NEW, T> onFulfiled,
                                    Function<NEW, T> onRejected);
}
