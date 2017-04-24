/*
 * Copyright 2015 JIHU, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.giiwa.mq;

import org.giiwa.mq.MQ.Mode;

/**
 * The IStub Class used to define the APIs of Message stub
 * 
 * @author joe
 *
 */
public abstract class IStub {

  /**
   * the service name that will bind on ActiveMQ
   */
  protected String name;

  /**
   * Instantiates a new i stub.
   *
   * @param name
   *          the name
   */
  public IStub(String name) {
    this.name = name;
  }

  /**
   * Bind the stub on the MQ as Queue
   *
   * @throws Exception
   *           the exception
   */
  final public void bind() throws Exception {
    bind(Mode.QUEUE);
  }

  /**
   * Bind the stub on the ActiveMQ with the mode
   *
   * @param m
   *          the mode
   * @throws Exception
   *           the exception
   */
  final public void bind(Mode m) throws Exception {
    MQ.bind(name, this, m);
  }

  /**
   * Send message to destination
   *
   * @param to
   *          the destination queue
   * @param type
   *          the message type
   * @param message
   *          the message
   * @throws Exception
   *           the exception
   */
  public void send(String to, Request req) throws Exception {
    req.from = name;
    MQ.send(to, req);
  }

  /**
   * send a topic to the destination
   * 
   * @param to
   *          the destination topic
   * @param type
   *          the message type
   * @param message
   *          the message
   * @throws Exception
   *           the exception
   */
  public void topic(String to, Request req) throws Exception {
    req.from = name;
    MQ.topic(to, req);
  }

  /**
   * On request triggered when message arrive
   *
   * @param seq
   *          the sequence
   * @param from
   *          the source destination queue
   * @param type
   *          the message type
   * @param message
   *          the message
   */
  public abstract void onRequest(long seq, Request req);

}
