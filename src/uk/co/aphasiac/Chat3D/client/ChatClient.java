package uk.co.aphasiac.Chat3D.client;

import uk.co.aphasiac.Chat3D.common.ChatEntity;

public interface ChatClient extends ChatEntity
{
      public String getHost();

      public int getPort();

      public String getAlias();

      public void handleChat(String input);

      public void connect();

      public void disconnect();
}