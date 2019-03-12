/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.driver.internal.messaging.v4;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.neo4j.driver.internal.Bookmarks;
import org.neo4j.driver.internal.util.messaging.AbstractMessageWriterTestBase;
import org.neo4j.driver.internal.messaging.Message;
import org.neo4j.driver.internal.messaging.MessageFormat;
import org.neo4j.driver.internal.messaging.request.BeginMessage;
import org.neo4j.driver.internal.messaging.request.DiscardNMessage;
import org.neo4j.driver.internal.messaging.request.HelloMessage;
import org.neo4j.driver.internal.messaging.request.InitMessage;
import org.neo4j.driver.internal.messaging.request.PullNMessage;
import org.neo4j.driver.internal.messaging.request.RunMessage;
import org.neo4j.driver.internal.messaging.request.RunWithMetadataMessage;
import org.neo4j.driver.internal.packstream.PackOutput;
import org.neo4j.driver.internal.security.InternalAuthToken;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.neo4j.driver.internal.messaging.request.CommitMessage.COMMIT;
import static org.neo4j.driver.internal.messaging.request.DiscardAllMessage.DISCARD_ALL;
import static org.neo4j.driver.internal.messaging.request.GoodbyeMessage.GOODBYE;
import static org.neo4j.driver.internal.messaging.request.PullAllMessage.PULL_ALL;
import static org.neo4j.driver.internal.messaging.request.ResetMessage.RESET;
import static org.neo4j.driver.internal.messaging.request.RollbackMessage.ROLLBACK;
import static org.neo4j.driver.AccessMode.READ;
import static org.neo4j.driver.AccessMode.WRITE;
import static org.neo4j.driver.AuthTokens.basic;
import static org.neo4j.driver.Values.point;
import static org.neo4j.driver.Values.value;

class MessageWriterV4Test extends AbstractMessageWriterTestBase
{
    @Override
    protected MessageFormat.Writer newWriter( PackOutput output )
    {
        return new MessageWriterV4( output );
    }

    @Override
    protected Stream<Message> supportedMessages()
    {
        return Stream.of(

                // New Bolt V4 messages
                new PullNMessage( 100, 200 ),
                new DiscardNMessage( 300, 400 ),

                // Bolt V3 messages
                new HelloMessage( "MyDriver/1.2.3", ((InternalAuthToken) basic( "neo4j", "neo4j" )).toMap() ),
                GOODBYE,
                new BeginMessage( Bookmarks.from( "neo4j:bookmark:v1:tx123" ), Duration.ofSeconds( 5 ), singletonMap( "key", value( 42 ) ), READ ),
                new BeginMessage( Bookmarks.from( "neo4j:bookmark:v1:tx123" ), Duration.ofSeconds( 5 ), singletonMap( "key", value( 42 ) ), WRITE ),
                COMMIT,
                ROLLBACK,

                RESET,
                new RunWithMetadataMessage( "RETURN 1", emptyMap(), Bookmarks.from( "neo4j:bookmark:v1:tx1" ), Duration.ofSeconds( 5 ),
                        singletonMap( "key", value( 42 ) ), READ ),
                new RunWithMetadataMessage( "RETURN 1", emptyMap(), Bookmarks.from( "neo4j:bookmark:v1:tx1" ), Duration.ofSeconds( 5 ),
                        singletonMap( "key", value( 42 ) ), WRITE ),

                // Bolt V3 messages with struct values
                new RunWithMetadataMessage( "RETURN $x", singletonMap( "x", value( ZonedDateTime.now() ) ), Bookmarks.empty(),
                        Duration.ofSeconds( 1 ), emptyMap(), READ ),
                new RunWithMetadataMessage( "RETURN $x", singletonMap( "x", value( ZonedDateTime.now() ) ), Bookmarks.empty(),
                        Duration.ofSeconds( 1 ), emptyMap(), WRITE ),
                new RunWithMetadataMessage( "RETURN $x", singletonMap( "x", point( 42, 1, 2, 3 ) ), Bookmarks.empty(),
                        Duration.ofSeconds( 1 ), emptyMap(), READ ),
                new RunWithMetadataMessage( "RETURN $x", singletonMap( "x", point( 42, 1, 2, 3 ) ), Bookmarks.empty(),
                        Duration.ofSeconds( 1 ), emptyMap(), WRITE )
        );
    }

    @Override
    protected Stream<Message> unsupportedMessages()
    {
        return Stream.of(
                // Bolt V1, V2 and V3 messages
                new InitMessage( "Apa", emptyMap() ),
                new RunMessage( "RETURN 1" ),
                PULL_ALL,
                DISCARD_ALL
        );
    }
}
