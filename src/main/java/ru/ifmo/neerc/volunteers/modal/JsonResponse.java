package ru.ifmo.neerc.volunteers.modal;

import lombok.Data;

/**
 * Created by Lapenok Akesej on 08.07.2017.
 */
@Data
public class JsonResponse<T> {
    Status status;
    T result;
}
