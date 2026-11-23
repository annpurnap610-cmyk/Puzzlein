import {
  ArrowLeft,
  Pause,
  Play,
  RotateCcw,
  Undo2,
  Volume2,
  VolumeX,
} from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { BannerAd } from './components/BannerAd';
import { DifficultyScreen } from './components/DifficultyScreen';
import { PuzzleBoard } from './components/PuzzleBoard';
import { VictoryModal } from './components/VictoryModal';
import {
  DIFFICULTY_OPTIONS,
  GameState,
  GridSize,
  MoveDirection,
  RecordsBySize,
} from './types/puzzle';
import {
  countPlacedTiles,
  createShuffledBoard,
  formatTime,
  isBoardSolved,
  loadBestRecords,
  saveBestRecord,
  slideByDirection,
  slideTileAtIndex,
} from './utils/puzzleLogic';
import { playSlideSound, playVictorySound } from './utils/sound';

export default function App() {
  const [gameState, setGameState] = useState<GameState>('TITLE_MENU');
  const [selectedSize, setSelectedSize] = useState<GridSize>(4);
  const [board, setBoard] = useState<number[]>(() => createShuffledBoard(4, 280));
  const [history, setHistory] = useState<number[][]>([]);
  const [moves, setMoves] = useState<number>(0);
  const [elapsedSeconds, setElapsedSeconds] = useState<number>(0);
  const [hasStartedMoving, setHasStartedMoving] = useState<boolean>(false);
  const [soundEnabled, setSoundEnabled] = useState<boolean>(true);
  const [records, setRecords] = useState<RecordsBySize>(() => loadBestRecords());
  const [victoryBadges, setVictoryBadges] = useState<{
    isNewBestMoves: boolean;
    isNewBestTime: boolean;
  }>({ isNewBestMoves: false, isNewBestTime: false });

  const activeConfig =
    DIFFICULTY_OPTIONS.find((opt) => opt.size === selectedSize) ?? DIFFICULTY_OPTIONS[1];

  const startNewGame = useCallback(
    (sizeToStart: GridSize = selectedSize) => {
      const config =
        DIFFICULTY_OPTIONS.find((opt) => opt.size === sizeToStart) ?? DIFFICULTY_OPTIONS[1];
      setSelectedSize(sizeToStart);
      setBoard(createShuffledBoard(sizeToStart, config.scrambleMoves));
      setHistory([]);
      setMoves(0);
      setElapsedSeconds(0);
      setHasStartedMoving(false);
      setVictoryBadges({ isNewBestMoves: false, isNewBestTime: false });
      setGameState('PLAYING');
    },
    [selectedSize]
  );

  const returnToDifficultyScreen = useCallback(() => {
    setGameState('TITLE_MENU');
  }, []);

  const applyBoardMove = useCallback(
    (nextBoard: number[] | null) => {
      if (!nextBoard || gameState !== 'PLAYING') return;

      const nextMoves = moves + 1;
      const prevPlaced = countPlacedTiles(board);
      const nextPlaced = countPlacedTiles(nextBoard);

      setHistory((prev) => [...prev, board]);
      setBoard(nextBoard);
      setMoves(nextMoves);
      if (!hasStartedMoving) {
        setHasStartedMoving(true);
      }

      if (isBoardSolved(nextBoard)) {
        const result = saveBestRecord(selectedSize, nextMoves, elapsedSeconds);
        setRecords(result.records);
        setVictoryBadges({
          isNewBestMoves: result.isNewBestMoves,
          isNewBestTime: result.isNewBestTime,
        });
        setGameState('VICTORY');
        playVictorySound(soundEnabled);
      } else {
        playSlideSound(soundEnabled, nextPlaced > prevPlaced);
      }
    },
    [board, elapsedSeconds, gameState, hasStartedMoving, moves, selectedSize, soundEnabled]
  );

  const handleTileClick = useCallback(
    (tileIndex: number) => {
      const nextBoard = slideTileAtIndex(board, tileIndex, selectedSize);
      applyBoardMove(nextBoard);
    },
    [applyBoardMove, board, selectedSize]
  );

  const handleUndo = useCallback(() => {
    if (gameState !== 'PLAYING' || history.length === 0) return;
    const previousBoard = history[history.length - 1];
    setHistory((prev) => prev.slice(0, -1));
    setBoard(previousBoard);
    setMoves((prev) => Math.max(0, prev - 1));
    playSlideSound(soundEnabled, false);
  }, [gameState, history, soundEnabled]);

  // Game clock ticks only during active play after the first tile move
  useEffect(() => {
    if (gameState !== 'PLAYING' || !hasStartedMoving) {
      return;
    }

    const timer = window.setInterval(() => {
      setElapsedSeconds((prev) => prev + 1);
    }, 1000);

    return () => window.clearInterval(timer);
  }, [gameState, hasStartedMoving]);

  // Keyboard support for puzzle sliding, pause, restart, and difficulty screen start
  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.metaKey || event.ctrlKey || event.altKey) return;

      if (gameState === 'TITLE_MENU') {
        if (event.key === '1') setSelectedSize(3);
        if (event.key === '2') setSelectedSize(4);
        if (event.key === '3') setSelectedSize(5);
        return;
      }

      if (event.key === 'Escape' || event.key.toLowerCase() === 'p') {
        if (gameState === 'PLAYING') {
          event.preventDefault();
          setGameState('PAUSED');
        } else if (gameState === 'PAUSED') {
          event.preventDefault();
          setGameState('PLAYING');
        }
        return;
      }

      if (event.key.toLowerCase() === 'r' && (gameState === 'PLAYING' || gameState === 'PAUSED')) {
        event.preventDefault();
        startNewGame(selectedSize);
        return;
      }

      if (gameState !== 'PLAYING') return;

      const keyToDir: Record<string, MoveDirection> = {
        ArrowUp: 'UP',
        w: 'UP',
        W: 'UP',
        ArrowDown: 'DOWN',
        s: 'DOWN',
        S: 'DOWN',
        ArrowLeft: 'LEFT',
        a: 'LEFT',
        A: 'LEFT',
        ArrowRight: 'RIGHT',
        d: 'RIGHT',
        D: 'RIGHT',
      };

      const direction = keyToDir[event.key];
      if (direction) {
        event.preventDefault();
        const nextBoard = slideByDirection(board, selectedSize, direction);
        applyBoardMove(nextBoard);
      }
    };

    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [applyBoardMove, board, gameState, selectedSize, startNewGame]);

  const placedCount = countPlacedTiles(board);
  const totalTiles = selectedSize * selectedSize - 1;
  const activeBest = records[selectedSize];

  return (
    <div className="flex min-h-screen flex-col bg-slate-950 text-slate-100">
      <header className="border-b border-slate-800/80 bg-slate-950/90">
        <div className="mx-auto flex max-w-5xl items-center justify-between gap-8 px-4 py-4 sm:px-6">
          <button
            type="button"
            onClick={returnToDifficultyScreen}
            className="font-display text-xl font-bold tracking-tight text-slate-50 transition-colors hover:text-amber-400 whitespace-nowrap shrink-0 cursor-pointer"
          >
            SlideGrid
          </button>

          <nav aria-label="Grid shortcuts" className="hidden items-center gap-6 sm:flex">
            {DIFFICULTY_OPTIONS.map((option) => (
              <button
                key={option.size}
                type="button"
                onClick={() => {
                  if (gameState === 'TITLE_MENU') {
                    setSelectedSize(option.size);
                  } else {
                    startNewGame(option.size);
                  }
                }}
                className={`text-sm font-medium transition-colors whitespace-nowrap shrink-0 cursor-pointer ${
                  selectedSize === option.size
                    ? 'text-amber-400 underline decoration-amber-400/60 underline-offset-4'
                    : 'text-slate-400 hover:text-slate-100'
                }`}
              >
                {option.size}×{option.size} {option.label}
              </button>
            ))}
          </nav>

          <button
            type="button"
            onClick={() => setSoundEnabled((prev) => !prev)}
            aria-label={soundEnabled ? 'Mute sound effects' : 'Enable sound effects'}
            className="inline-flex min-h-[40px] items-center gap-2 rounded-lg border border-slate-800 bg-slate-900 px-3.5 py-2 text-xs font-medium text-slate-200 transition-colors hover:border-slate-700 hover:bg-slate-800 whitespace-nowrap shrink-0 cursor-pointer"
          >
            {soundEnabled ? (
              <Volume2 className="h-4 w-4 text-amber-400" aria-hidden="true" />
            ) : (
              <VolumeX className="h-4 w-4 text-slate-500" aria-hidden="true" />
            )}
            <span>{soundEnabled ? 'Sound On' : 'Muted'}</span>
          </button>
        </div>
      </header>

      <main className="mx-auto flex w-full max-w-5xl flex-1 flex-col justify-center px-4 py-8 sm:px-6 sm:py-10">
        {gameState === 'TITLE_MENU' ? (
          <DifficultyScreen
            selectedSize={selectedSize}
            records={records}
            onSelectSize={setSelectedSize}
            onStartGame={startNewGame}
          />
        ) : (
          <div className="mx-auto flex w-full max-w-[440px] flex-col gap-5">
            {/* Compact HUD */}
            <div className="flex items-center justify-between gap-3">
              <button
                type="button"
                onClick={returnToDifficultyScreen}
                className="inline-flex min-h-[40px] items-center gap-1.5 rounded-lg border border-slate-800 bg-slate-900 px-3 py-2 text-xs font-medium text-slate-300 transition-colors hover:border-slate-700 hover:text-slate-50 whitespace-nowrap shrink-0 cursor-pointer"
              >
                <ArrowLeft className="h-3.5 w-3.5" aria-hidden="true" />
                <span>Difficulty</span>
              </button>

              <div className="text-right">
                <p className="font-display text-base font-bold text-slate-50">
                  {activeConfig.title} · {activeConfig.label}
                </p>
                <p className="font-mono text-xs text-slate-400 tabular-nums">
                  {activeBest
                    ? `Best ${activeBest.bestMoves} moves · ${formatTime(activeBest.bestTimeSeconds)}`
                    : 'No record yet'}
                </p>
              </div>
            </div>

            {/* Score & Progress Strip */}
            <div className="grid grid-cols-3 divide-x divide-slate-800 rounded-xl border border-slate-800 bg-slate-900/80 py-3">
              <div className="flex flex-col items-center gap-0.5 px-2">
                <span className="text-xs text-slate-400">Moves</span>
                <span className="font-mono text-lg font-bold text-slate-50 tabular-nums">
                  {moves}
                </span>
              </div>
              <div className="flex flex-col items-center gap-0.5 px-2">
                <span className="text-xs text-slate-400">Time</span>
                <span className="font-mono text-lg font-bold text-slate-50 tabular-nums">
                  {formatTime(elapsedSeconds)}
                </span>
              </div>
              <div className="flex flex-col items-center gap-0.5 px-2">
                <span className="text-xs text-slate-400">Placed</span>
                <span className="font-mono text-lg font-bold text-emerald-400 tabular-nums">
                  {placedCount}/{totalTiles}
                </span>
              </div>
            </div>

            {/* Active Puzzle Grid */}
            <PuzzleBoard
              board={board}
              size={selectedSize}
              gameState={gameState}
              onTileClick={handleTileClick}
              onResume={() => setGameState('PLAYING')}
              onRestart={() => startNewGame(selectedSize)}
              onChangeDifficulty={returnToDifficultyScreen}
            />

            {/* Board Controls */}
            <div className="grid grid-cols-3 gap-2.5">
              <button
                type="button"
                onClick={handleUndo}
                disabled={history.length === 0 || gameState !== 'PLAYING'}
                className="inline-flex min-h-[44px] items-center justify-center gap-1.5 rounded-lg border border-slate-800 bg-slate-900 px-3 py-2 text-xs font-medium text-slate-200 transition-colors hover:border-slate-700 hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-45 whitespace-nowrap cursor-pointer"
              >
                <Undo2 className="h-3.5 w-3.5" aria-hidden="true" />
                <span>Undo</span>
              </button>

              <button
                type="button"
                onClick={() =>
                  setGameState((prev) => (prev === 'PAUSED' ? 'PLAYING' : 'PAUSED'))
                }
                disabled={gameState === 'VICTORY'}
                className="inline-flex min-h-[44px] items-center justify-center gap-1.5 rounded-lg border border-slate-800 bg-slate-900 px-3 py-2 text-xs font-medium text-slate-200 transition-colors hover:border-slate-700 hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-45 whitespace-nowrap cursor-pointer"
              >
                {gameState === 'PAUSED' ? (
                  <>
                    <Play className="h-3.5 w-3.5 text-amber-400" aria-hidden="true" />
                    <span>Resume</span>
                  </>
                ) : (
                  <>
                    <Pause className="h-3.5 w-3.5" aria-hidden="true" />
                    <span>Pause</span>
                  </>
                )}
              </button>

              <button
                type="button"
                onClick={() => startNewGame(selectedSize)}
                className="inline-flex min-h-[44px] items-center justify-center gap-1.5 rounded-lg border border-slate-800 bg-slate-900 px-3 py-2 text-xs font-medium text-slate-200 transition-colors hover:border-slate-700 hover:bg-slate-800 whitespace-nowrap cursor-pointer"
              >
                <RotateCcw className="h-3.5 w-3.5" aria-hidden="true" />
                <span>Shuffle</span>
              </button>
            </div>

            <p className="text-center text-xs text-slate-500">
              {hasStartedMoving
                ? 'Press P to pause · R to shuffle · Arrow keys or click to slide'
                : 'Timer starts on your first move'}
            </p>
          </div>
        )}
      </main>

      {gameState === 'VICTORY' && (
        <VictoryModal
          size={selectedSize}
          moves={moves}
          elapsedSeconds={elapsedSeconds}
          isNewBestMoves={victoryBadges.isNewBestMoves}
          isNewBestTime={victoryBadges.isNewBestTime}
          onPlayAgain={() => startNewGame(selectedSize)}
          onChangeDifficulty={returnToDifficultyScreen}
        />
      )}

      <BannerAd />
    </div>
  );
}
