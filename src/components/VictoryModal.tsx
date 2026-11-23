import { RotateCcw, SlidersHorizontal, Trophy } from 'lucide-react';
import { GridSize } from '../types/puzzle';
import { formatTime } from '../utils/puzzleLogic';

interface VictoryModalProps {
  size: GridSize;
  moves: number;
  elapsedSeconds: number;
  isNewBestMoves: boolean;
  isNewBestTime: boolean;
  onPlayAgain: () => void;
  onChangeDifficulty: () => void;
}

export function VictoryModal({
  size,
  moves,
  elapsedSeconds,
  isNewBestMoves,
  isNewBestTime,
  onPlayAgain,
  onChangeDifficulty,
}: VictoryModalProps) {
  const achievedRecord = isNewBestMoves || isNewBestTime;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="victory-title"
      className="fixed inset-0 z-30 flex items-center justify-center bg-slate-950/80 p-4 backdrop-blur-sm"
    >
      <div className="w-full max-w-md rounded-2xl border border-slate-800 bg-slate-900 p-6 shadow-2xl sm:p-7">
        <div className="flex flex-col gap-5">
          <div className="flex items-start justify-between gap-4">
            <div className="flex flex-col gap-1">
              <span className="text-xs font-semibold text-emerald-400">
                Puzzle Solved · {size} × {size} Grid
              </span>
              <h2
                id="victory-title"
                className="font-display text-2xl font-bold text-slate-50 sm:text-3xl"
              >
                Board Complete
              </h2>
            </div>
            <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-slate-700 bg-slate-800 text-amber-400">
              <Trophy className="h-5 w-5" aria-hidden="true" />
            </div>
          </div>

          {achievedRecord && (
            <p className="text-sm font-medium text-amber-300">
              New personal record set
              {isNewBestMoves && isNewBestTime
                ? ' for fewest moves and fastest time.'
                : isNewBestMoves
                  ? ' for fewest moves.'
                  : ' for fastest time.'}
            </p>
          )}

          <div className="grid grid-cols-2 gap-3 border-y border-slate-800 py-4">
            <div className="flex flex-col gap-1">
              <span className="text-xs text-slate-400">Total Moves</span>
              <span className="font-mono text-2xl font-bold text-slate-50 tabular-nums">
                {moves}
              </span>
              {isNewBestMoves && (
                <span className="text-xs font-medium text-amber-400">Personal Best</span>
              )}
            </div>
            <div className="flex flex-col gap-1">
              <span className="text-xs text-slate-400">Elapsed Time</span>
              <span className="font-mono text-2xl font-bold text-slate-50 tabular-nums">
                {formatTime(elapsedSeconds)}
              </span>
              {isNewBestTime && (
                <span className="text-xs font-medium text-amber-400">Personal Best</span>
              )}
            </div>
          </div>

          <div className="flex flex-col gap-2.5 sm:flex-row">
            <button
              type="button"
              onClick={onPlayAgain}
              className="inline-flex min-h-[44px] flex-1 items-center justify-center gap-2 rounded-lg bg-amber-400 px-4 py-2.5 text-sm font-semibold text-slate-950 transition-colors hover:bg-amber-300 whitespace-nowrap cursor-pointer"
            >
              <RotateCcw className="h-4 w-4" aria-hidden="true" />
              <span>Play Again</span>
            </button>
            <button
              type="button"
              onClick={onChangeDifficulty}
              className="inline-flex min-h-[44px] flex-1 items-center justify-center gap-2 rounded-lg border border-slate-700 bg-slate-800 px-4 py-2.5 text-sm font-medium text-slate-100 transition-colors hover:bg-slate-700 whitespace-nowrap cursor-pointer"
            >
              <SlidersHorizontal className="h-4 w-4" aria-hidden="true" />
              <span>Choose Difficulty</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
