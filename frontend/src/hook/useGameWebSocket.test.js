import { renderHook, act } from "@testing-library/react-hooks";
import { Server } from "mock-socket";
import useGameWebSocket from "./useGameWebSocket";

describe("useGameWebSocket", () => {
  let mockServer;

  beforeEach(() => {
    mockServer = new Server("ws://localhost:8080/ws/game/testGameId");
  });

  afterEach(() => {
    mockServer.close();
  });

  it("should update userIds and usernames when it received a userJoined message", () => {
    const { result } = renderHook(() => useGameWebSocket("testGameId"));
    act(() => {
      mockServer.emit(
        "message",
        JSON.stringify({
          type: "USER_JOINED",
          data: {
            "userId1": "userName1",
            "userId2": "userName2",
          },
        })
      );
    });
    expect(result.current.userIds).toEqual(["userId1", "userId2"]);
    expect(result.current.usernames).toEqual(["userName1", "userName2"]);
  });

  it("should not update when message types is not userJoined", () => {
    const { result } = renderHook(() => useGameWebSocket("testGameId"));

    act(() => {
      mockServer.emit(
        "message",
        JSON.stringify({
          type: "otherMessageType",
          data: {
            "userId1": "userName1",
            "userId2": "userName2",
          },
        })
      );
    });
    expect(result.current.userIds).toEqual([]);
    expect(result.current.usernames).toEqual([]);
  });
});



