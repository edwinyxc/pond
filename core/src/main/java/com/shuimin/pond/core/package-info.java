/**
 * <h6>Pond Server </h6>
 *
 *
 * <p>Core API</p>
 * <ul>
 *     <li>Pond
 *      - Service Domain, API entrance,
 *      responsible for life-cycle management of executions,
 *      controls all executable middlewares,
 *      hold an complex of configurations,
 *      running at master thread,
 *      itself must be singleton & immutable.</li>
 *
 *     <li>Middleware
 *      - Entity Domain, Prototype, representing specified functions.
 *      Any Object declared as a middleware can be managed by Server,
 *      Middleware has no states, thread-safe.
 *      Any call to the server will trigger a Middleware string
 *      to finish a job. The scale of individual Middleware usually
 *      very limited and specified.
 *     </li>
 *
 *     <li>ExecutionContext
 *      - Session Domain, Thread-Local variable
 *      holding any mutable states of execution.
 *      From the very beginning to the end, ExecutionContext
 *      holds everything, especially the result of recent middleware.
 *      </li>
 *
 * </ul>
 *
 * <p>Core</p>
 *
 */
package com.shuimin.pond.core;