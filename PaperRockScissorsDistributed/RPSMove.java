import java.nio.ByteBuffer;

public class RPSMove
{
	private final String move;
	private final String playerName;
	
	RPSMove(String name, String move)
	{
		this.move = move;
		playerName = name;
	}
	
	RPSMove(byte[] objectBytes)
	{
		int numPlayerNameBytes = ByteBuffer.wrap(objectBytes, 0, 4).getInt();
		playerName = new String(objectBytes, 4, numPlayerNameBytes);
		int numMoveBytes = ByteBuffer.wrap(objectBytes, 4 + numPlayerNameBytes, 4).getInt();
		move = new String(objectBytes, 8 + numPlayerNameBytes, numMoveBytes);
	}
	
	RPSMove(byte[] objectBytes, int offset)
	{
		int numPlayerNameBytes = ByteBuffer.wrap(objectBytes, offset, 4).getInt();
		playerName = new String(objectBytes, offset + 4, numPlayerNameBytes);
		int numMoveBytes = ByteBuffer.wrap(objectBytes, offset + 4 + numPlayerNameBytes, 4).getInt();
		move = new String(objectBytes, offset + 8 + numPlayerNameBytes, numMoveBytes);
	}
	
	public String getPlayer()
	{
		return playerName;
	}
	
	public String getMove()
	{
		return move;
	}
	
	public byte[] toByteArray()
	{
		byte[] playerNameBytes = playerName.getBytes();
		byte[] moveBytes = move.getBytes();
		byte[] numPlayerNameBytes = intToBytes(playerNameBytes.length);
		byte[] numMoveBytes = intToBytes(moveBytes.length);
		byte[] objectSizeBytes = intToBytes(playerNameBytes.length + moveBytes.length + numPlayerNameBytes.length + numMoveBytes.length + 4);
		byte[] buffer = new byte[playerNameBytes.length + moveBytes.length + numPlayerNameBytes.length + numMoveBytes.length + objectSizeBytes.length];
		
		for (int x = 0; x < objectSizeBytes.length; x++)
			buffer[x] = objectSizeBytes[x];
		for (int x = objectSizeBytes.length; x < objectSizeBytes.length + numPlayerNameBytes.length; x++)
			buffer[x] = numPlayerNameBytes[x - objectSizeBytes.length];
		for (int x = objectSizeBytes.length + numPlayerNameBytes.length; x < objectSizeBytes.length + numPlayerNameBytes.length + playerNameBytes.length; x++)
			buffer[x] = playerNameBytes[x - (objectSizeBytes.length + numPlayerNameBytes.length)];
		for (int x = objectSizeBytes.length + numPlayerNameBytes.length + playerNameBytes.length; x < objectSizeBytes.length + numPlayerNameBytes.length + playerNameBytes.length + numMoveBytes.length; x++)
			buffer[x] = numMoveBytes[x - (objectSizeBytes.length + numPlayerNameBytes.length + playerNameBytes.length)];
		for (int x = objectSizeBytes.length + numPlayerNameBytes.length + playerNameBytes.length + numMoveBytes.length; x < objectSizeBytes.length + numPlayerNameBytes.length + playerNameBytes.length + numMoveBytes.length + moveBytes.length; x++)
			buffer[x] = moveBytes[x - (objectSizeBytes.length + numPlayerNameBytes.length + playerNameBytes.length + numMoveBytes.length)];

		return buffer;
	}
	
	public String toString()
	{
		StringBuilder obj = new StringBuilder();
		obj.append("Player Name: " + playerName + "\n");
		obj.append("Move: " + move);
		return obj.toString();
	}
	
	private byte[] intToBytes(int i)
	{
		byte[] b = new byte[4];		
		b[0] = (byte) (i >> 24);
		i <<= 8;
		b[1] = (byte) (i >> 24);
		i <<= 8;
		b[2] = (byte) (i >> 24);
		i <<= 8;
		b[3] = (byte) (i >> 24);
		
		return b;
	}
}
