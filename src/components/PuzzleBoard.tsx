import { Pause, Play, RotateCcw, SlidersHorizontal } from 'lucide-react';
import { GameState, GridSize } from '../types/puzzle';
import { canSlideTile } from '../utils/puzzleLogic';

interface PuzzleBoardProps {
  board: number[];
  size: GridSize;
  gameState: GameState;
  onTileClick: (index: number) => void;
  onResume: () => void;
  onRestart: () => void;
  onChangeDifficulty: () => void;
}

export function PuzzleBoard({
  board,
  size,
  gameState,
  onTileClick,
  onResume,
  onRestart,
  onChangeDifficulty,
}: PuzzleBoardProps) {
  const totalSlots = size * size;
  const tiles = Array.from({ length: totalSlots - 1 }, (_, idx) => idx + 1);

  const fontSizeClass =
    size === 3
      ? 'text-3xl sm:text-4xl'
      : size === 4
        ? 'text-2xl sm:text-3xl'
        : 'text-lg sm:text-2xl';

  const padClass = size === 5 ? 'p-1 sm:p-1.5' : 'p-1.5 sm:p-2';

  return (
    <div className="relative mx-auto w-full max-w-[440px]">
      <div
        role="grid"
        aria-label={`${size} by ${size} sliding number puzzle board`}
        className="relative aspect-square w-full rounded-2xl border border-slate-800 bg-slate-900 p-2.5 shadow-xl sm:p-3.5"
      >
        {/* Recessed background slots */}
        <div
          aria-hidden="true"
          className="grid h-full w-full"
          style={{
            gridTemplateColumns: `repeat(${size}, minmax(0, 1fr))`,
            gridTemplateRows: `repeat(${size}, minmax(0, 1fr))`,
          }}
        >
          {Array.from({ length: totalSlots }, (_, slotIndex) => (
            <div key={slotIndex} className={padClass}>
              <div className="h-full w-full rounded-xl border border-slate-800/80 bg-slate-950/75" />
            </div>
          ))}
        </div>

        {/* Interactive sliding tiles positioned via compositor translate3d */}
        <div className="absolute inset-2.5 sm:inset-3.5">
          {tiles.map((tileValue) => {
            const currentIndex = board.indexOf(tileValue);
            const row = Math.floor(currentIndex / size);
            const col = currentIndex % size;
            const isMovable =
              gameState === 'PLAYING' && canSlideTile(board, currentIndex, size);
            const isCorrectSpot = currentIndex === tileValue - 1;

            return (
              <div
                key={tileValue}
                className={`tile-transition absolute top-0 left-0 transition-transform duration-150 ease-[cubic-bezier(0.16,1,0.3,1)] ${padClass}`}
                style={{
                  width: `${100 / size}%`,
                  height: `${100 / size}%`,
                  transform: `translate3d(${col * 100}%, ${row * 100}%, 0)`,
                }}
              >
                <button
                  type="button"
                  disabled={!isMovable}
                  onClick={() => onTileClick(currentIndex)}
                  aria-label={`Tile ${tileValue}, row ${row + 1} column ${col + 1}${
                    isCorrectSpot ? ', in target position' : ''
                  }`}
                  className={`relative flex h-full w-full select-none items-center justify-center rounded-xl border font-mono font-bold tabular-nums transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-amber-400 focus-visible:ring-offset-1 focus-visible:ring-offset-slate-900 ${fontSizeClass} ${
                    isCorrectSpot
                      ? 'border-emerald-300 bg-emerald-50 text-slate-950 shadow-sm'
                      : 'border-slate-200 bg-slate-100 text-slate-900 shadow-sm'
                  } ${
                    isMovable
                      ? 'cursor-pointer hover:bg-white active:scale-[0.97]'
                      : 'cursor-default'
                  }`}
                >
                  <span>{tileValue}</span>
                  {isCorrectSpot && (
                    <span
                      aria-hidden="true"
                      className="absolute right-2 top-2 h-1.5 w-1.5 rounded-full bg-emerald-600"
                    />
                  )}
                </button>
              </div>
            );
          })}
        </div>

        {/* Pause overlay hides tile values to keep timing fair */}
        {gameState === 'PAUSED' && (
          <div
            role="dialog"
            aria-modal="true"
            aria-label="Game paused"
            className="absolute inset-0 z-10 flex flex-col items-center justify-center gap-5 rounded-2xl bg-slate-950/92 p-6 text-center backdrop-blur-sm"
          >
            <div className="flex h-12 w-12 items-center justify-center rounded-xl border border-slate-800 bg-slate-900 text-amber-400">
              <Pause className="h-5 w-5" aria-hidden="true" />
            </div>
            <div className="flex flex-col gap-1">
              <h2 className="font-display text-2xl font-bold text-slate-50">Puzzle Paused</h2>
              <p className="text-sm text-slate-400">Timer is stopped while tiles are hidden.</p>
            </div>
            <div className="flex w-full max-w-[240px] flex-col gap-2.5">
              <button
                type="button"
                onClick={onResume}
                className="inline-flex min-h-[44px] items-center justify-center gap-2 rounded-lg bg-amber-400 px-4 py-2 text-sm font-semibold text-slate-950 transition-colors hover:bg-amber-300 cursor-pointer"
              >
                <Play className="h-4 w-4" aria-hidden="true" />
                <span>Resume Puzzle</span>
              </button>
              <button
                type="button"
                onClick={onRestart}
                className="inline-flex min-h-[44px] items-center justify-center gap-2 rounded-lg border border-slate-700 bg-slate-900 px-4 py-2 text-sm font-medium text-slate-200 transition-colors hover:bg-slate-800 cursor-pointer"
              >
                <RotateCcw className="h-4 w-4" aria-hidden="true" />
                <span>Restart {size}×{size}</span>
              </button>
              <button
                type="button"
                onClick={onChangeDifficulty}
                className="inline-flex min-h-[44px] items-center justify-center gap-2 rounded-lg border border-slate-800 bg-transparent px-4 py-2 text-sm font-medium text-slate-300 transition-colors hover:bg-slate-900 cursor-pointer"
              >
                <SlidersHorizontal className="h-4 w-4" aria-hidden="true" />
                <span>Change Difficulty</span>
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
